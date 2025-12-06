package com.example.kantinkampus;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CheckoutActivity extends AppCompatActivity {
    private DBHelper dbHelper;
    private EditText etCustomerName, etNotes;
    private RadioGroup rgPaymentMethod;
    private TextView tvTotal, btnConfirmOrder;
    private CardView btnBack;
    private int totalAmount;
    private String standName = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        dbHelper = new DBHelper(this);

        // Initialize views
        etCustomerName = findViewById(R.id.etCustomerName);
        etNotes = findViewById(R.id.etNotes);
        rgPaymentMethod = findViewById(R.id.rgPaymentMethod);
        tvTotal = findViewById(R.id.tvTotal);
        btnConfirmOrder = findViewById(R.id.btnConfirmOrder);
        btnBack = findViewById(R.id.btnBack);

        // Calculate total and get stand name
        calculateTotal();

        // Setup buttons
        btnConfirmOrder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processOrder();
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void calculateTotal() {
        List<CartItem> cartItems = dbHelper.getCartItems();
        totalAmount = 0;

        if (!cartItems.isEmpty()) {
            // Get stand name from first item
            standName = getStandNameByMenuId(cartItems.get(0).getMenu().getStandId());
        }

        for (CartItem item : cartItems) {
            totalAmount += item.getSubtotal();
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        tvTotal.setText(formatter.format(totalAmount));
    }

    private String getStandNameByMenuId(int standId) {
        List<Stand> stands = dbHelper.getAllStands();
        for (Stand stand : stands) {
            if (stand.getId() == standId) {
                return stand.getNama();
            }
        }
        return "Stand";
    }

    private void processOrder() {
        String customerName = etCustomerName.getText().toString().trim();
        String notes = etNotes.getText().toString().trim();

        // Validation
        if (customerName.isEmpty()) {
            etCustomerName.setError("Nama tidak boleh kosong");
            etCustomerName.requestFocus();
            return;
        }

        // Get selected payment method
        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Pilih metode pembayaran", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedPayment = findViewById(selectedPaymentId);
        String paymentMethod = selectedPayment.getText().toString();

        // Get cart items
        List<CartItem> cartItems = dbHelper.getCartItems();
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Keranjang kosong", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Build items string
        StringBuilder itemsBuilder = new StringBuilder();
        for (CartItem item : cartItems) {
            itemsBuilder.append(item.getMenu().getNama())
                    .append(" (")
                    .append(item.getQty())
                    .append("x)\n");
        }
        String items = itemsBuilder.toString();

        // Show confirmation dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("✅ Konfirmasi Pesanan");

        NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String message = "Nama: " + customerName + "\n" +
                "Stand: " + standName + "\n" +
                "Pembayaran: " + paymentMethod + "\n" +
                "Total: " + formatter.format(totalAmount) + "\n\n" +
                "Lanjutkan pesanan?";

        builder.setMessage(message);

        builder.setPositiveButton("Ya, Pesan! 🍽️", (dialog, which) -> {
            // Create order
            long orderId = dbHelper.createOrder(
                    customerName,
                    standName,
                    items,
                    totalAmount,
                    paymentMethod,
                    notes
            );

            // Add order items
            for (CartItem item : cartItems) {
                dbHelper.addOrderItem(
                        (int) orderId,
                        item.getMenu().getId(),
                        item.getMenu().getNama(),
                        item.getQty(),
                        item.getMenu().getHarga()
                );
            }

            // Add to history (for backward compatibility)
            dbHelper.addToHistory(items, totalAmount);

            // Clear cart
            dbHelper.clearCart();

            // Show success message
            Toast.makeText(CheckoutActivity.this,
                    "✅ Pesanan berhasil! Order ID: #" + orderId,
                    Toast.LENGTH_LONG).show();

            // Finish activities and return to main
            setResult(RESULT_OK);
            finish();
        });

        builder.setNegativeButton("Batal", null);
        builder.show();
    }
}