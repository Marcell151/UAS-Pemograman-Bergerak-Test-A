package com.example.kantinkampus;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * CHECKOUT ACTIVITY - Payment First System
 * Handles payment method selection and order creation
 */
public class CheckoutActivity extends AppCompatActivity {
    private RadioGroup rgPaymentMethod;
    private RadioButton rbCash, rbOvo, rbGopay, rbDana, rbTransfer;
    private LinearLayout layoutTransferInfo;
    private TextView tvTotal, btnPlaceOrder, tvOrderSummary;
    private EditText etPaymentProof;

    private DBHelper dbHelper;
    private SessionManager sessionManager;
    private int totalAmount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        // Initialize
        dbHelper = new DBHelper(this);
        sessionManager = new SessionManager(this);

        // Get total from intent
        totalAmount = getIntent().getIntExtra("total", 0);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Pembayaran");
        }

        // Initialize views
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        rbCash = findViewById(R.id.rbCash);
        rbOvo = findViewById(R.id.rbOvo);
        rbGopay = findViewById(R.id.rbGopay);
        rbDana = findViewById(R.id.rbDana);
        rbTransfer = findViewById(R.id.rbTransfer);
        layoutTransferInfo = findViewById(R.id.layoutTransferInfo);
        tvTotal = findViewById(R.id.tvTotal);
        btnPlaceOrder = findViewById(R.id.btnPlaceOrder);
        tvOrderSummary = findViewById(R.id.tvOrderSummary);
        etPaymentProof = findViewById(R.id.etPaymentProof);

        // Display total
        tvTotal.setText(formatPrice(totalAmount));

        // Load order summary
        loadOrderSummary();

        // Payment method selection
        rgPaymentMethod.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbTransfer) {
                layoutTransferInfo.setVisibility(View.VISIBLE);
            } else {
                layoutTransferInfo.setVisibility(View.GONE);
            }
        });

        // Place order button
        btnPlaceOrder.setOnClickListener(v -> confirmOrder());
    }

    private void loadOrderSummary() {
        int userId = sessionManager.getUserId();
        List<CartItem> cartItems = dbHelper.getCartItems(userId);

        StringBuilder summary = new StringBuilder();
        summary.append("📦 Ringkasan Pesanan:\n\n");

        // Group by stand
        String currentStand = "";
        for (CartItem item : cartItems) {
            Menu menu = item.getMenu();
            if (!currentStand.equals(String.valueOf(menu.getStandId()))) {
                if (!currentStand.isEmpty()) {
                    summary.append("\n");
                }
                currentStand = String.valueOf(menu.getStandId());
                // Get stand name (simplified - should get from DB)
                summary.append("🏪 Stand ").append(currentStand).append("\n");
            }

            summary.append("  • ").append(menu.getNama())
                    .append(" x").append(item.getQty())
                    .append(" = ").append(formatPrice(item.getSubtotal())).append("\n");
        }

        summary.append("\n💰 Total: ").append(formatPrice(totalAmount));

        tvOrderSummary.setText(summary.toString());
    }

    private void confirmOrder() {
        // Validate payment method
        int selectedId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran terlebih dahulu", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRb = findViewById(selectedId);
        String paymentMethod = selectedRb.getText().toString();

        // If transfer, validate payment proof
        String paymentProof = null;
        if (selectedId == R.id.rbTransfer) {
            paymentProof = etPaymentProof.getText().toString().trim();
            if (paymentProof.isEmpty()) {
                etPaymentProof.setError("Upload bukti pembayaran");
                etPaymentProof.requestFocus();
                return;
            }
        }

        // Show confirmation
        String message = "Metode Pembayaran: " + paymentMethod + "\n" +
                "Total: " + formatPrice(totalAmount) + "\n\n";

        if (selectedId == R.id.rbCash) {
            message += "💵 Bayar tunai saat mengambil pesanan.\n" +
                    "Pesanan akan diverifikasi oleh penjual.";
        } else {
            message += "📱 Pesanan akan diverifikasi setelah penjual memeriksa pembayaran.";
        }

        String finalPaymentProof = paymentProof;
        new AlertDialog.Builder(this)
                .setTitle("Konfirmasi Pesanan")
                .setMessage(message)
                .setPositiveButton("Ya, Pesan", (dialog, which) -> {
                    processOrder(paymentMethod, finalPaymentProof);
                })
                .setNegativeButton("Batal", null)
                .show();
    }

    private void processOrder(String paymentMethod, String paymentProof) {
        int buyerId = sessionManager.getUserId();

        // Create orders (grouped by stand)
        List<Long> orderIds = dbHelper.createOrdersFromCart(buyerId, paymentMethod);

        if (!orderIds.isEmpty()) {
            // If transfer, update payment proof
            if (paymentProof != null) {
                for (Long orderId : orderIds) {
                    dbHelper.updatePaymentProof(orderId.intValue(), paymentProof);
                }
            }

            // Show success
            String title = orderIds.size() > 1 ?
                    "✅ " + orderIds.size() + " Pesanan Berhasil Dibuat!" :
                    "✅ Pesanan Berhasil Dibuat!";

            String message = paymentMethod.equals("💵 Cash") ?
                    "Silakan bayar tunai saat mengambil pesanan.\n" +
                            "Penjual akan memverifikasi pesanan Anda." :
                    "Pesanan Anda sedang menunggu verifikasi pembayaran.\n" +
                            "Anda akan diberi notifikasi setelah diverifikasi.";

            new AlertDialog.Builder(this)
                    .setTitle(title)
                    .setMessage(message)
                    .setPositiveButton("Lihat Pesanan", (dialog, which) -> {
                        // Redirect to orders
                        Intent intent = new Intent(CheckoutActivity.this, BuyerOrdersActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelable(false)
                    .show();

        } else {
            Toast.makeText(this, "Gagal membuat pesanan. Coba lagi.", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatPrice(int price) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return formatter.format(price).replace("IDR", "Rp").replace(",00", "");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}