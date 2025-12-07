package com.example.kantinkampus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import java.util.Map;
import java.util.LinkedHashMap;

/**
 * KANTIN KAMPUS - COMPLETE DATABASE HELPER
 * Version: 3.0 - Complete Rebuild
 *
 * MAJOR CHANGES:
 * 1. Dual Role System (Seller & Buyer)
 * 2. One Stand Per Seller Policy
 * 3. Multi-Stand Orders Support
 * 4. Payment First System
 * 5. Order Verification by Seller
 */
public class DBHelper extends SQLiteOpenHelper {
    private static final String TAG = "DBHelper";
    private static final String DATABASE_NAME = "kantinkampus_v3.db";
    private static final int DATABASE_VERSION = 3;

    // ==================== TABLES ====================

    // Table: Users (Unified for Seller & Buyer)
    private static final String TABLE_USERS = "users";
    private static final String USER_ID = "id";
    private static final String USER_EMAIL = "email";
    private static final String USER_PASSWORD = "password";
    private static final String USER_NAME = "name";
    private static final String USER_ROLE = "role"; // 'seller' atau 'buyer'
    private static final String USER_PHONE = "phone";
    private static final String USER_ID_NUMBER = "id_number"; // Kartu Usaha OR NIM/NIP
    private static final String USER_TYPE = "type"; // 'mahasiswa', 'dosen', 'staff' (untuk buyer) | null untuk seller
    private static final String USER_CREATED_AT = "created_at";

    // Table: Stands (One per Seller)
    private static final String TABLE_STAND = "stands";
    private static final String STAND_ID = "id";
    private static final String STAND_SELLER_ID = "seller_id"; // FK to users
    private static final String STAND_NAME = "name";
    private static final String STAND_DESCRIPTION = "description";
    private static final String STAND_IMAGE = "image";
    private static final String STAND_CREATED_AT = "created_at";

    // Table: Menus
    private static final String TABLE_MENU = "menus";
    private static final String MENU_ID = "id";
    private static final String MENU_STAND_ID = "stand_id";
    private static final String MENU_NAME = "name";
    private static final String MENU_PRICE = "price";
    private static final String MENU_IMAGE = "image";
    private static final String MENU_DESCRIPTION = "description";
    private static final String MENU_CATEGORY = "category";
    private static final String MENU_STATUS = "status"; // 'available', 'unavailable'
    private static final String MENU_CREATED_AT = "created_at";

    // Table: Cart (Support Multi-Stand)
    private static final String TABLE_CART = "cart";
    private static final String CART_ID = "id";
    private static final String CART_BUYER_ID = "buyer_id";
    private static final String CART_MENU_ID = "menu_id";
    private static final String CART_QTY = "qty";
    private static final String CART_NOTES = "notes";

    // Table: Orders (Grouped by Stand)
    private static final String TABLE_ORDERS = "orders";
    private static final String ORDER_ID = "id";
    private static final String ORDER_BUYER_ID = "buyer_id";
    private static final String ORDER_STAND_ID = "stand_id";
    private static final String ORDER_TOTAL = "total";
    private static final String ORDER_STATUS = "status";
    // 'pending_payment', 'pending_verification', 'verified', 'cooking', 'ready', 'completed', 'cancelled'
    private static final String ORDER_PAYMENT_METHOD = "payment_method"; // 'cash', 'ovo', 'gopay'
    private static final String ORDER_PAYMENT_PROOF = "payment_proof"; // URL/path bukti transfer
    private static final String ORDER_PAYMENT_STATUS = "payment_status"; // 'unpaid', 'pending', 'verified', 'rejected'
    private static final String ORDER_SELLER_NOTES = "seller_notes"; // Alasan reject/notes
    private static final String ORDER_BUYER_NOTES = "buyer_notes";
    private static final String ORDER_CREATED_AT = "created_at";
    private static final String ORDER_UPDATED_AT = "updated_at";

    // Table: Order Items
    private static final String TABLE_ORDER_ITEMS = "order_items";
    private static final String ITEM_ID = "id";
    private static final String ITEM_ORDER_ID = "order_id";
    private static final String ITEM_MENU_ID = "menu_id";
    private static final String ITEM_QTY = "qty";
    private static final String ITEM_PRICE = "price";
    private static final String ITEM_SUBTOTAL = "subtotal";

    // Table: Favorites
    private static final String TABLE_FAVORITES = "favorites";
    private static final String FAV_ID = "id";
    private static final String FAV_BUYER_ID = "buyer_id";
    private static final String FAV_MENU_ID = "menu_id";
    private static final String FAV_CREATED_AT = "created_at";

    // Table: Reviews
    private static final String TABLE_REVIEWS = "reviews";
    private static final String REVIEW_ID = "id";
    private static final String REVIEW_BUYER_ID = "buyer_id";
    private static final String REVIEW_MENU_ID = "menu_id";
    private static final String REVIEW_ORDER_ID = "order_id";
    private static final String REVIEW_RATING = "rating";
    private static final String REVIEW_COMMENT = "comment";
    private static final String REVIEW_CREATED_AT = "created_at";

    // Table: Notifications
    private static final String TABLE_NOTIFICATIONS = "notifications";
    private static final String NOTIF_ID = "id";
    private static final String NOTIF_USER_ID = "user_id";
    private static final String NOTIF_TYPE = "type"; // 'order_placed', 'payment_verified', 'order_ready', etc
    private static final String NOTIF_TITLE = "title";
    private static final String NOTIF_MESSAGE = "message";
    private static final String NOTIF_ORDER_ID = "order_id";
    private static final String NOTIF_IS_READ = "is_read";
    private static final String NOTIF_CREATED_AT = "created_at";

    private Context context;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            // Create Users Table
            String createUsers = "CREATE TABLE " + TABLE_USERS + " ("
                    + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + USER_EMAIL + " TEXT UNIQUE NOT NULL, "
                    + USER_PASSWORD + " TEXT NOT NULL, "
                    + USER_NAME + " TEXT NOT NULL, "
                    + USER_ROLE + " TEXT NOT NULL CHECK(" + USER_ROLE + " IN ('seller', 'buyer')), "
                    + USER_PHONE + " TEXT NOT NULL, "
                    + USER_ID_NUMBER + " TEXT NOT NULL, "
                    + USER_TYPE + " TEXT, "
                    + USER_CREATED_AT + " TEXT NOT NULL)";
            db.execSQL(createUsers);

            // Create Stands Table (with constraint: one stand per seller)
            String createStands = "CREATE TABLE " + TABLE_STAND + " ("
                    + STAND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + STAND_SELLER_ID + " INTEGER NOT NULL UNIQUE, "
                    + STAND_NAME + " TEXT NOT NULL, "
                    + STAND_DESCRIPTION + " TEXT, "
                    + STAND_IMAGE + " TEXT, "
                    + STAND_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + STAND_SELLER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE)";
            db.execSQL(createStands);

            // Create Menus Table
            String createMenus = "CREATE TABLE " + TABLE_MENU + " ("
                    + MENU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + MENU_STAND_ID + " INTEGER NOT NULL, "
                    + MENU_NAME + " TEXT NOT NULL, "
                    + MENU_PRICE + " INTEGER NOT NULL, "
                    + MENU_IMAGE + " TEXT, "
                    + MENU_DESCRIPTION + " TEXT, "
                    + MENU_CATEGORY + " TEXT, "
                    + MENU_STATUS + " TEXT DEFAULT 'available' CHECK(" + MENU_STATUS + " IN ('available', 'unavailable')), "
                    + MENU_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + MENU_STAND_ID + ") REFERENCES " + TABLE_STAND + "(" + STAND_ID + ") ON DELETE CASCADE)";
            db.execSQL(createMenus);

            // Create Cart Table
            String createCart = "CREATE TABLE " + TABLE_CART + " ("
                    + CART_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + CART_BUYER_ID + " INTEGER NOT NULL, "
                    + CART_MENU_ID + " INTEGER NOT NULL, "
                    + CART_QTY + " INTEGER NOT NULL CHECK(" + CART_QTY + " > 0), "
                    + CART_NOTES + " TEXT, "
                    + "FOREIGN KEY(" + CART_BUYER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + CART_MENU_ID + ") REFERENCES " + TABLE_MENU + "(" + MENU_ID + ") ON DELETE CASCADE, "
                    + "UNIQUE(" + CART_BUYER_ID + ", " + CART_MENU_ID + "))";
            db.execSQL(createCart);

            // Create Orders Table
            String createOrders = "CREATE TABLE " + TABLE_ORDERS + " ("
                    + ORDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ORDER_BUYER_ID + " INTEGER NOT NULL, "
                    + ORDER_STAND_ID + " INTEGER NOT NULL, "
                    + ORDER_TOTAL + " INTEGER NOT NULL, "
                    + ORDER_STATUS + " TEXT DEFAULT 'pending_payment', "
                    + ORDER_PAYMENT_METHOD + " TEXT, "
                    + ORDER_PAYMENT_PROOF + " TEXT, "
                    + ORDER_PAYMENT_STATUS + " TEXT DEFAULT 'unpaid', "
                    + ORDER_SELLER_NOTES + " TEXT, "
                    + ORDER_BUYER_NOTES + " TEXT, "
                    + ORDER_CREATED_AT + " TEXT NOT NULL, "
                    + ORDER_UPDATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + ORDER_BUYER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "), "
                    + "FOREIGN KEY(" + ORDER_STAND_ID + ") REFERENCES " + TABLE_STAND + "(" + STAND_ID + "))";
            db.execSQL(createOrders);

            // Create Order Items Table
            String createOrderItems = "CREATE TABLE " + TABLE_ORDER_ITEMS + " ("
                    + ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + ITEM_ORDER_ID + " INTEGER NOT NULL, "
                    + ITEM_MENU_ID + " INTEGER NOT NULL, "
                    + ITEM_QTY + " INTEGER NOT NULL, "
                    + ITEM_PRICE + " INTEGER NOT NULL, "
                    + ITEM_SUBTOTAL + " INTEGER NOT NULL, "
                    + "FOREIGN KEY(" + ITEM_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + ORDER_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + ITEM_MENU_ID + ") REFERENCES " + TABLE_MENU + "(" + MENU_ID + "))";
            db.execSQL(createOrderItems);

            // Create Favorites Table
            String createFavorites = "CREATE TABLE " + TABLE_FAVORITES + " ("
                    + FAV_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + FAV_BUYER_ID + " INTEGER NOT NULL, "
                    + FAV_MENU_ID + " INTEGER NOT NULL, "
                    + FAV_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + FAV_BUYER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + FAV_MENU_ID + ") REFERENCES " + TABLE_MENU + "(" + MENU_ID + ") ON DELETE CASCADE, "
                    + "UNIQUE(" + FAV_BUYER_ID + ", " + FAV_MENU_ID + "))";
            db.execSQL(createFavorites);

            // Create Reviews Table
            String createReviews = "CREATE TABLE " + TABLE_REVIEWS + " ("
                    + REVIEW_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + REVIEW_BUYER_ID + " INTEGER NOT NULL, "
                    + REVIEW_MENU_ID + " INTEGER NOT NULL, "
                    + REVIEW_ORDER_ID + " INTEGER, "
                    + REVIEW_RATING + " INTEGER NOT NULL CHECK(" + REVIEW_RATING + " >= 1 AND " + REVIEW_RATING + " <= 5), "
                    + REVIEW_COMMENT + " TEXT, "
                    + REVIEW_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + REVIEW_BUYER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "), "
                    + "FOREIGN KEY(" + REVIEW_MENU_ID + ") REFERENCES " + TABLE_MENU + "(" + MENU_ID + "), "
                    + "FOREIGN KEY(" + REVIEW_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + ORDER_ID + "))";
            db.execSQL(createReviews);

            // Create Notifications Table
            String createNotifications = "CREATE TABLE " + TABLE_NOTIFICATIONS + " ("
                    + NOTIF_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                    + NOTIF_USER_ID + " INTEGER NOT NULL, "
                    + NOTIF_TYPE + " TEXT NOT NULL, "
                    + NOTIF_TITLE + " TEXT NOT NULL, "
                    + NOTIF_MESSAGE + " TEXT NOT NULL, "
                    + NOTIF_ORDER_ID + " INTEGER, "
                    + NOTIF_IS_READ + " INTEGER DEFAULT 0, "
                    + NOTIF_CREATED_AT + " TEXT NOT NULL, "
                    + "FOREIGN KEY(" + NOTIF_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + ") ON DELETE CASCADE, "
                    + "FOREIGN KEY(" + NOTIF_ORDER_ID + ") REFERENCES " + TABLE_ORDERS + "(" + ORDER_ID + ") ON DELETE CASCADE)";
            db.execSQL(createNotifications);

            // Insert Demo Data
            insertDemoData(db);

            Log.d(TAG, "✅ Database created successfully!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating database: " + e.getMessage(), e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);

        // Drop all tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTIFICATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_REVIEWS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDER_ITEMS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ORDERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CART);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MENU);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STAND);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);

        // Recreate
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    // ==================== DEMO DATA ====================

    private void insertDemoData(SQLiteDatabase db) {
        String timestamp = getCurrentTimestamp();

        try {
            // Insert Demo Seller 1
            ContentValues seller1 = new ContentValues();
            seller1.put(USER_EMAIL, "seller1@kantin.com");
            seller1.put(USER_PASSWORD, "seller123");
            seller1.put(USER_NAME, "Ibu Sari");
            seller1.put(USER_ROLE, "seller");
            seller1.put(USER_PHONE, "081234567890");
            seller1.put(USER_ID_NUMBER, "KARTU-001");
            seller1.put(USER_CREATED_AT, timestamp);
            long seller1Id = db.insert(TABLE_USERS, null, seller1);

            // Insert Demo Seller 2
            ContentValues seller2 = new ContentValues();
            seller2.put(USER_EMAIL, "seller2@kantin.com");
            seller2.put(USER_PASSWORD, "seller123");
            seller2.put(USER_NAME, "Pak Danu");
            seller2.put(USER_ROLE, "seller");
            seller2.put(USER_PHONE, "082345678901");
            seller2.put(USER_ID_NUMBER, "KARTU-002");
            seller2.put(USER_CREATED_AT, timestamp);
            long seller2Id = db.insert(TABLE_USERS, null, seller2);

            // Insert Demo Buyer - Mahasiswa
            ContentValues buyer1 = new ContentValues();
            buyer1.put(USER_EMAIL, "mahasiswa@example.com");
            buyer1.put(USER_PASSWORD, "buyer123");
            buyer1.put(USER_NAME, "Budi Santoso");
            buyer1.put(USER_ROLE, "buyer");
            buyer1.put(USER_PHONE, "083456789012");
            buyer1.put(USER_ID_NUMBER, "123456789"); // NIM
            buyer1.put(USER_TYPE, "mahasiswa");
            buyer1.put(USER_CREATED_AT, timestamp);
            db.insert(TABLE_USERS, null, buyer1);

            // Insert Demo Buyer - Dosen
            ContentValues buyer2 = new ContentValues();
            buyer2.put(USER_EMAIL, "dosen@example.com");
            buyer2.put(USER_PASSWORD, "buyer123");
            buyer2.put(USER_NAME, "Dr. Siti Nurhaliza");
            buyer2.put(USER_ROLE, "buyer");
            buyer2.put(USER_PHONE, "084567890123");
            buyer2.put(USER_ID_NUMBER, "198501012010012001"); // NIP
            buyer2.put(USER_TYPE, "dosen");
            buyer2.put(USER_CREATED_AT, timestamp);
            db.insert(TABLE_USERS, null, buyer2);

            // Insert Stand 1 (Seller 1)
            ContentValues stand1 = new ContentValues();
            stand1.put(STAND_SELLER_ID, seller1Id);
            stand1.put(STAND_NAME, "Warung Nasi Bu Sari");
            stand1.put(STAND_DESCRIPTION, "Makanan Berat - Nasi & Lauk");
            stand1.put(STAND_CREATED_AT, timestamp);
            long stand1Id = db.insert(TABLE_STAND, null, stand1);

            // Insert Stand 2 (Seller 2)
            ContentValues stand2 = new ContentValues();
            stand2.put(STAND_SELLER_ID, seller2Id);
            stand2.put(STAND_NAME, "Minuman Fresh Pak Danu");
            stand2.put(STAND_DESCRIPTION, "Minuman Segar & Kopi");
            stand2.put(STAND_CREATED_AT, timestamp);
            long stand2Id = db.insert(TABLE_STAND, null, stand2);

            // Insert Menus for Stand 1
            insertDemoMenu(db, (int) stand1Id, "Nasi Ayam Geprek", 18000, "Nasi putih dengan ayam geprek pedas", "Makanan Berat", timestamp);
            insertDemoMenu(db, (int) stand1Id, "Nasi Ayam Bakar", 20000, "Nasi dengan ayam bakar bumbu kecap", "Makanan Berat", timestamp);
            insertDemoMenu(db, (int) stand1Id, "Nasi Telur Balado", 12000, "Nasi putih dengan telur balado", "Makanan Berat", timestamp);
            insertDemoMenu(db, (int) stand1Id, "Nasi Lele Goreng", 16000, "Nasi dengan lele goreng crispy", "Makanan Berat", timestamp);
            insertDemoMenu(db, (int) stand1Id, "Nasi Ayam Kremes", 17000, "Nasi dengan ayam kremes kriuk", "Makanan Berat", timestamp);

            // Insert Menus for Stand 2
            insertDemoMenu(db, (int) stand2Id, "Es Teh Manis", 5000, "Teh manis dingin segar", "Minuman", timestamp);
            insertDemoMenu(db, (int) stand2Id, "Es Jeruk Fresh", 7000, "Jeruk peras asli tanpa pengawet", "Minuman", timestamp);
            insertDemoMenu(db, (int) stand2Id, "Kopi Hitam", 8000, "Kopi hitam original", "Minuman", timestamp);
            insertDemoMenu(db, (int) stand2Id, "Thai Tea", 10000, "Teh Thailand khas", "Minuman", timestamp);
            insertDemoMenu(db, (int) stand2Id, "Matcha Latte", 12000, "Minuman matcha creamy", "Minuman", timestamp);

            Log.d(TAG, "✅ Demo data inserted successfully!");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error inserting demo data: " + e.getMessage(), e);
        }
    }

    private void insertDemoMenu(SQLiteDatabase db, int standId, String name, int price,
                                String description, String category, String timestamp) {
        ContentValues values = new ContentValues();
        values.put(MENU_STAND_ID, standId);
        values.put(MENU_NAME, name);
        values.put(MENU_PRICE, price);
        values.put(MENU_DESCRIPTION, description);
        values.put(MENU_CATEGORY, category);
        values.put(MENU_STATUS, "available");
        values.put(MENU_CREATED_AT, timestamp);
        db.insert(TABLE_MENU, null, values);
    }

    private String getCurrentTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    // ==================== USER AUTHENTICATION ====================

    /**
     * Register new user (Seller or Buyer)
     * @param email Email address
     * @param password Password (plain text - should be hashed in production)
     * @param name Full name
     * @param role 'seller' or 'buyer'
     * @param phone Phone number
     * @param idNumber Kartu Usaha (seller) or NIM/NIP (buyer)
     * @param type null for seller, 'mahasiswa'/'dosen'/'staff' for buyer
     * @return user ID if success, -1 if email exists, -2 if error
     */
    public long registerUser(String email, String password, String name, String role,
                             String phone, String idNumber, String type) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Check if email exists
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + " = ?",
                    new String[]{email});

            if (cursor.getCount() > 0) {
                cursor.close();
                return -1; // Email already exists
            }
            cursor.close();

            // Insert new user
            ContentValues values = new ContentValues();
            values.put(USER_EMAIL, email);
            values.put(USER_PASSWORD, password); // TODO: Hash password in production!
            values.put(USER_NAME, name);
            values.put(USER_ROLE, role);
            values.put(USER_PHONE, phone);
            values.put(USER_ID_NUMBER, idNumber);
            values.put(USER_TYPE, type);
            values.put(USER_CREATED_AT, getCurrentTimestamp());

            long userId = db.insert(TABLE_USERS, null, values);

            if (userId > 0) {
                Log.d(TAG, "✅ User registered: " + email + " as " + role);
            }

            return userId;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error registering user: " + e.getMessage(), e);
            return -2;
        }
    }

    /**
     * Login user
     * @return User object if success, null if failed
     */
    public User loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_USERS + " WHERE " + USER_EMAIL + " = ? AND " + USER_PASSWORD + " = ?",
                    new String[]{email, password}
            );

            User user = null;
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(USER_ROLE)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE)));
                user.setNimNip(cursor.getString(cursor.getColumnIndexOrThrow(USER_ID_NUMBER)));
                user.setType(cursor.getString(cursor.getColumnIndexOrThrow(USER_TYPE)));

                Log.d(TAG, "✅ User logged in: " + email);
            } else {
                Log.d(TAG, "❌ Login failed for: " + email);
            }

            cursor.close();
            return user;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error during login: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get user by ID
     */
    public User getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + USER_ID + " = ?",
                    new String[]{String.valueOf(userId)});

            User user = null;
            if (cursor.moveToFirst()) {
                user = new User();
                user.setId(cursor.getInt(cursor.getColumnIndexOrThrow(USER_ID)));
                user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)));
                user.setName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                user.setRole(cursor.getString(cursor.getColumnIndexOrThrow(USER_ROLE)));
                user.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(USER_PHONE)));
                user.setNimNip(cursor.getString(cursor.getColumnIndexOrThrow(USER_ID_NUMBER)));
                user.setType(cursor.getString(cursor.getColumnIndexOrThrow(USER_TYPE)));
            }
            cursor.close();
            return user;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting user: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== STAND MANAGEMENT (SELLER ONLY) ====================

    /**
     * Create stand (ONE per seller only)
     * @return stand ID if success, -1 if seller already has stand, -2 if error
     */
    public long createStand(int sellerId, String name, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Check if seller already has a stand
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STAND + " WHERE " + STAND_SELLER_ID + " = ?",
                    new String[]{String.valueOf(sellerId)});

            if (cursor.getCount() > 0) {
                cursor.close();
                Log.w(TAG, "⚠️ Seller already has a stand!");
                return -1; // Already has stand
            }
            cursor.close();

            // Create new stand
            ContentValues values = new ContentValues();
            values.put(STAND_SELLER_ID, sellerId);
            values.put(STAND_NAME, name);
            values.put(STAND_DESCRIPTION, description);
            values.put(STAND_IMAGE, image);
            values.put(STAND_CREATED_AT, getCurrentTimestamp());

            long standId = db.insert(TABLE_STAND, null, values);

            if (standId > 0) {
                Log.d(TAG, "✅ Stand created: " + name);
            }

            return standId;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating stand: " + e.getMessage(), e);
            return -2;
        }
    }

    /**
     * Get stand by seller ID
     */
    public Stand getStandBySeller(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STAND + " WHERE " + STAND_SELLER_ID + " = ?",
                    new String[]{String.valueOf(sellerId)});

            Stand stand = null;
            if (cursor.moveToFirst()) {
                stand = new Stand();
                stand.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_ID)));
                stand.setNama(cursor.getString(cursor.getColumnIndexOrThrow(STAND_NAME)));
                stand.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(STAND_DESCRIPTION)));
                stand.setImage(cursor.getString(cursor.getColumnIndexOrThrow(STAND_IMAGE)));
                stand.setOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_SELLER_ID)));
            }
            cursor.close();
            return stand;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting stand: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Update stand info
     */
    public int updateStand(int standId, String name, String description, String image) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(STAND_NAME, name);
            values.put(STAND_DESCRIPTION, description);
            if (image != null) {
                values.put(STAND_IMAGE, image);
            }

            int rows = db.update(TABLE_STAND, values, STAND_ID + " = ?",
                    new String[]{String.valueOf(standId)});

            if (rows > 0) {
                Log.d(TAG, "✅ Stand updated");
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating stand: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get all stands (for buyers)
     */
    public List<Stand> getAllStands() {
        List<Stand> stands = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT s.*, u." + USER_NAME + " as seller_name, u." + USER_PHONE + " as seller_phone " +
                    "FROM " + TABLE_STAND + " s " +
                    "INNER JOIN " + TABLE_USERS + " u ON s." + STAND_SELLER_ID + " = u." + USER_ID + " " +
                    "ORDER BY s." + STAND_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Stand stand = new Stand();
                    stand.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_ID)));
                    stand.setNama(cursor.getString(cursor.getColumnIndexOrThrow(STAND_NAME)));
                    stand.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(STAND_DESCRIPTION)));
                    stand.setImage(cursor.getString(cursor.getColumnIndexOrThrow(STAND_IMAGE)));
                    stand.setOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_SELLER_ID)));
                    stands.add(stand);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting stands: " + e.getMessage(), e);
        }

        return stands;
    }

    // ==================== MENU MANAGEMENT ====================

    /**
     * Add menu to stand
     */
    public long addMenu(int standId, String name, int price, String image,
                        String description, String category) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(MENU_STAND_ID, standId);
            values.put(MENU_NAME, name);
            values.put(MENU_PRICE, price);
            values.put(MENU_IMAGE, image);
            values.put(MENU_DESCRIPTION, description);
            values.put(MENU_CATEGORY, category);
            values.put(MENU_STATUS, "available");
            values.put(MENU_CREATED_AT, getCurrentTimestamp());

            long menuId = db.insert(TABLE_MENU, null, values);

            if (menuId > 0) {
                Log.d(TAG, "✅ Menu added: " + name);
            }

            return menuId;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding menu: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Update menu
     */
    public int updateMenu(int menuId, String name, int price, String image,
                          String description, String category, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(MENU_NAME, name);
            values.put(MENU_PRICE, price);
            if (image != null) {
                values.put(MENU_IMAGE, image);
            }
            values.put(MENU_DESCRIPTION, description);
            values.put(MENU_CATEGORY, category);
            values.put(MENU_STATUS, status);

            int rows = db.update(TABLE_MENU, values, MENU_ID + " = ?",
                    new String[]{String.valueOf(menuId)});

            if (rows > 0) {
                Log.d(TAG, "✅ Menu updated");
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating menu: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Delete menu
     */
    public int deleteMenu(int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            int rows = db.delete(TABLE_MENU, MENU_ID + " = ?",
                    new String[]{String.valueOf(menuId)});

            if (rows > 0) {
                Log.d(TAG, "✅ Menu deleted");
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error deleting menu: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get menus by stand ID
     */
    public List<Menu> getMenusByStand(int standId) {
        List<Menu> menus = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT m.*, " +
                    "COALESCE(AVG(r." + REVIEW_RATING + "), 0) as avg_rating, " +
                    "COUNT(DISTINCT r." + REVIEW_ID + ") as total_reviews " +
                    "FROM " + TABLE_MENU + " m " +
                    "LEFT JOIN " + TABLE_REVIEWS + " r ON m." + MENU_ID + " = r." + REVIEW_MENU_ID + " " +
                    "WHERE m." + MENU_STAND_ID + " = ? " +
                    "GROUP BY m." + MENU_ID + " " +
                    "ORDER BY m." + MENU_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(standId)});

            if (cursor.moveToFirst()) {
                do {
                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_STAND_ID)));
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setImage(cursor.getString(cursor.getColumnIndexOrThrow(MENU_IMAGE)));
                    menu.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(MENU_DESCRIPTION)));
                    menu.setKategori(cursor.getString(cursor.getColumnIndexOrThrow(MENU_CATEGORY)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));
                    menu.setAverageRating(cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating")));
                    menu.setTotalReviews(cursor.getInt(cursor.getColumnIndexOrThrow("total_reviews")));
                    menus.add(menu);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting menus: " + e.getMessage(), e);
        }

        return menus;
    }

    /**
     * Get menu by ID
     */
    public Menu getMenuById(int menuId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_MENU + " WHERE " + MENU_ID + " = ?",
                    new String[]{String.valueOf(menuId)});

            Menu menu = null;
            if (cursor.moveToFirst()) {
                menu = new Menu();
                menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_STAND_ID)));
                menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                menu.setImage(cursor.getString(cursor.getColumnIndexOrThrow(MENU_IMAGE)));
                menu.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(MENU_DESCRIPTION)));
                menu.setKategori(cursor.getString(cursor.getColumnIndexOrThrow(MENU_CATEGORY)));
                menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));
            }
            cursor.close();
            return menu;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting menu: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== CART MANAGEMENT (MULTI-STAND SUPPORT) ====================

    /**
     * Add item to cart
     */
    public long addToCart(int buyerId, int menuId, int qty, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            // Check if item already in cart
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CART +
                            " WHERE " + CART_BUYER_ID + " = ? AND " + CART_MENU_ID + " = ?",
                    new String[]{String.valueOf(buyerId), String.valueOf(menuId)});

            if (cursor.getCount() > 0) {
                // Update quantity
                cursor.moveToFirst();
                int currentQty = cursor.getInt(cursor.getColumnIndexOrThrow(CART_QTY));
                cursor.close();

                ContentValues values = new ContentValues();
                values.put(CART_QTY, currentQty + qty);
                if (notes != null) {
                    values.put(CART_NOTES, notes);
                }

                int rows = db.update(TABLE_CART, values,
                        CART_BUYER_ID + " = ? AND " + CART_MENU_ID + " = ?",
                        new String[]{String.valueOf(buyerId), String.valueOf(menuId)});

                return rows;
            } else {
                cursor.close();

                // Insert new item
                ContentValues values = new ContentValues();
                values.put(CART_BUYER_ID, buyerId);
                values.put(CART_MENU_ID, menuId);
                values.put(CART_QTY, qty);
                values.put(CART_NOTES, notes);

                return db.insert(TABLE_CART, null, values);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding to cart: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Get cart items grouped by stand
     */
    public List<CartItem> getCartItems(int buyerId) {
        List<CartItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT c.*, m.*, s." + STAND_NAME + ", s." + STAND_ID + " " +
                    "FROM " + TABLE_CART + " c " +
                    "INNER JOIN " + TABLE_MENU + " m ON c." + CART_MENU_ID + " = m." + MENU_ID + " " +
                    "INNER JOIN " + TABLE_STAND + " s ON m." + MENU_STAND_ID + " = s." + STAND_ID + " " +
                    "WHERE c." + CART_BUYER_ID + " = ? " +
                    "ORDER BY s." + STAND_NAME + ", m." + MENU_NAME;

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

            if (cursor.moveToFirst()) {
                do {
                    CartItem item = new CartItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID)));
                    item.setUserId(buyerId);
                    item.setQty(cursor.getInt(cursor.getColumnIndexOrThrow(CART_QTY)));
                    item.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(CART_NOTES)));

                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_ID)));
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));

                    item.setMenu(menu);
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting cart items: " + e.getMessage(), e);
        }

        return items;
    }

    /**
     * Update cart item quantity
     */
    public int updateCartQty(int cartId, int qty) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            if (qty <= 0) {
                // Delete if qty is 0
                return db.delete(TABLE_CART, CART_ID + " = ?",
                        new String[]{String.valueOf(cartId)});
            } else {
                ContentValues values = new ContentValues();
                values.put(CART_QTY, qty);

                return db.update(TABLE_CART, values, CART_ID + " = ?",
                        new String[]{String.valueOf(cartId)});
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating cart: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Clear cart for buyer
     */
    public int clearCart(int buyerId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            return db.delete(TABLE_CART, CART_BUYER_ID + " = ?",
                    new String[]{String.valueOf(buyerId)});

        } catch (Exception e) {
            Log.e(TAG, "❌ Error clearing cart: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get cart count
     */
    public int getCartCount(int buyerId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery("SELECT SUM(" + CART_QTY + ") FROM " + TABLE_CART +
                            " WHERE " + CART_BUYER_ID + " = ?",
                    new String[]{String.valueOf(buyerId)});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting cart count: " + e.getMessage(), e);
            return 0;
        }
    }

    // ==================== ORDER MANAGEMENT (NEW SYSTEM) ====================

    /**
     * Create orders from cart (GROUP BY STAND)
     * Returns list of order IDs created (one per stand)
     */
    public List<Long> createOrdersFromCart(int buyerId, String paymentMethod) {
        List<Long> orderIds = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // Get cart items grouped by stand
            List<CartItem> cartItems = getCartItems(buyerId);

            // Group by stand
            java.util.Map<Integer, List<CartItem>> standGroups = new java.util.HashMap<>();
            for (CartItem item : cartItems) {
                int standId = item.getMenu().getStandId();
                if (!standGroups.containsKey(standId)) {
                    standGroups.put(standId, new ArrayList<>());
                }
                standGroups.get(standId).add(item);
            }

            // Create order for each stand
            for (java.util.Map.Entry<Integer, List<CartItem>> entry : standGroups.entrySet()) {
                int standId = entry.getKey();
                List<CartItem> items = entry.getValue();

                // Calculate total
                int total = 0;
                for (CartItem item : items) {
                    total += item.getSubtotal();
                }

                // Create order
                ContentValues orderValues = new ContentValues();
                orderValues.put(ORDER_BUYER_ID, buyerId);
                orderValues.put(ORDER_STAND_ID, standId);
                orderValues.put(ORDER_TOTAL, total);
                orderValues.put(ORDER_STATUS, "pending_payment");
                orderValues.put(ORDER_PAYMENT_METHOD, paymentMethod);
                orderValues.put(ORDER_PAYMENT_STATUS, "unpaid");
                orderValues.put(ORDER_CREATED_AT, getCurrentTimestamp());
                orderValues.put(ORDER_UPDATED_AT, getCurrentTimestamp());

                long orderId = db.insert(TABLE_ORDERS, null, orderValues);

                if (orderId > 0) {
                    // Insert order items
                    for (CartItem item : items) {
                        ContentValues itemValues = new ContentValues();
                        itemValues.put(ITEM_ORDER_ID, orderId);
                        itemValues.put(ITEM_MENU_ID, item.getMenu().getId());
                        itemValues.put(ITEM_QTY, item.getQty());
                        itemValues.put(ITEM_PRICE, item.getMenu().getHarga());
                        itemValues.put(ITEM_SUBTOTAL, item.getSubtotal());
                        db.insert(TABLE_ORDER_ITEMS, null, itemValues);
                    }

                    orderIds.add(orderId);
                }
            }

            // Clear cart
            clearCart(buyerId);

            db.setTransactionSuccessful();
            Log.d(TAG, "✅ Created " + orderIds.size() + " orders from cart");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating orders: " + e.getMessage(), e);
        } finally {
            db.endTransaction();
        }

        return orderIds;
    }

    /**
     * Update payment proof (after buyer pays)
     */
    public int updatePaymentProof(int orderId, String proofUrl) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ORDER_PAYMENT_PROOF, proofUrl);
            values.put(ORDER_PAYMENT_STATUS, "pending");
            values.put(ORDER_STATUS, "pending_verification");
            values.put(ORDER_UPDATED_AT, getCurrentTimestamp());

            int rows = db.update(TABLE_ORDERS, values, ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            if (rows > 0) {
                // Notify seller
                Order order = getOrderById(orderId);
                Stand stand = getStandById(order.getStandId());
                createNotification(stand.getOwnerId(), "order_placed",
                        "🔔 Pesanan Baru Masuk!",
                        "Ada pesanan baru menunggu verifikasi pembayaran",
                        orderId);
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating payment proof: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Verify payment (Seller accepts)
     */
    public int verifyPayment(int orderId, boolean accepted, String sellerNotes) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();

            if (accepted) {
                values.put(ORDER_PAYMENT_STATUS, "verified");
                values.put(ORDER_STATUS, "verified");
            } else {
                values.put(ORDER_PAYMENT_STATUS, "rejected");
                values.put(ORDER_STATUS, "cancelled");
            }

            if (sellerNotes != null) {
                values.put(ORDER_SELLER_NOTES, sellerNotes);
            }
            values.put(ORDER_UPDATED_AT, getCurrentTimestamp());

            int rows = db.update(TABLE_ORDERS, values, ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            if (rows > 0) {
                // Notify buyer
                Order order = getOrderById(orderId);
                String title = accepted ? "✅ Pembayaran Diterima" : "❌ Pembayaran Ditolak";
                String message = accepted ?
                        "Pesanan Anda sedang diproses" :
                        "Pembayaran tidak valid. " + (sellerNotes != null ? sellerNotes : "");

                createNotification(order.getUserId(),
                        accepted ? "payment_verified" : "payment_rejected",
                        title, message, orderId);
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error verifying payment: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Update order status (by Seller)
     */
    public int updateOrderStatus(int orderId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ORDER_STATUS, status);
            values.put(ORDER_UPDATED_AT, getCurrentTimestamp());

            int rows = db.update(TABLE_ORDERS, values, ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            if (rows > 0) {
                // Notify buyer based on status
                Order order = getOrderById(orderId);
                String title = "";
                String message = "";
                String notifType = "";

                switch (status) {
                    case "cooking":
                        title = "👨‍🍳 Pesanan Sedang Dimasak";
                        message = "Pesanan Anda sedang disiapkan";
                        notifType = "order_cooking";
                        break;
                    case "ready":
                        title = "✅ Pesanan Siap!";
                        message = "Pesanan Anda sudah siap diambil";
                        notifType = "order_ready";
                        break;
                    case "completed":
                        title = "🎉 Pesanan Selesai";
                        message = "Terima kasih telah memesan!";
                        notifType = "order_completed";
                        break;
                }

                if (!title.isEmpty()) {
                    createNotification(order.getUserId(), notifType, title, message, orderId);
                }
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error updating order status: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Cancel order (by Seller only)
     */
    public int cancelOrder(int orderId, String reason) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(ORDER_STATUS, "cancelled");
            values.put(ORDER_SELLER_NOTES, reason);
            values.put(ORDER_UPDATED_AT, getCurrentTimestamp());

            int rows = db.update(TABLE_ORDERS, values, ORDER_ID + " = ?",
                    new String[]{String.valueOf(orderId)});

            if (rows > 0) {
                // Notify buyer
                Order order = getOrderById(orderId);
                createNotification(order.getUserId(), "order_cancelled",
                        "❌ Pesanan Dibatalkan",
                        "Pesanan dibatalkan oleh penjual. Alasan: " + reason,
                        orderId);
            }

            return rows;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error cancelling order: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get orders by buyer
     */
    public List<Order> getOrdersByBuyer(int buyerId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, s." + STAND_NAME + ", u." + USER_NAME + " as buyer_name " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "INNER JOIN " + TABLE_USERS + " u ON o." + ORDER_BUYER_ID + " = u." + USER_ID + " " +
                    "WHERE o." + ORDER_BUYER_ID + " = ? " +
                    "ORDER BY o." + ORDER_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

            if (cursor.moveToFirst()) {
                do {
                    orders.add(mapCursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting buyer orders: " + e.getMessage(), e);
        }

        return orders;
    }

    /**
     * Get orders by seller (stand)
     */
    public List<Order> getOrdersBySeller(int sellerId) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, s." + STAND_NAME + ", u." + USER_NAME + " as buyer_name " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "INNER JOIN " + TABLE_USERS + " u ON o." + ORDER_BUYER_ID + " = u." + USER_ID + " " +
                    "WHERE s." + STAND_SELLER_ID + " = ? " +
                    "ORDER BY o." + ORDER_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId)});

            if (cursor.moveToFirst()) {
                do {
                    orders.add(mapCursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting seller orders: " + e.getMessage(), e);
        }

        return orders;
    }

    /**
     * Get orders by status (for seller)
     */
    public List<Order> getOrdersBySellerAndStatus(int sellerId, String status) {
        List<Order> orders = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, s." + STAND_NAME + ", u." + USER_NAME + " as buyer_name " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "INNER JOIN " + TABLE_USERS + " u ON o." + ORDER_BUYER_ID + " = u." + USER_ID + " " +
                    "WHERE s." + STAND_SELLER_ID + " = ? AND o." + ORDER_STATUS + " = ? " +
                    "ORDER BY o." + ORDER_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId), status});

            if (cursor.moveToFirst()) {
                do {
                    orders.add(mapCursorToOrder(cursor));
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting orders by status: " + e.getMessage(), e);
        }

        return orders;
    }

    /**
     * Get order by ID
     */
    public Order getOrderById(int orderId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT o.*, s." + STAND_NAME + ", u." + USER_NAME + " as buyer_name " +
                    "FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "INNER JOIN " + TABLE_USERS + " u ON o." + ORDER_BUYER_ID + " = u." + USER_ID + " " +
                    "WHERE o." + ORDER_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

            Order order = null;
            if (cursor.moveToFirst()) {
                order = mapCursorToOrder(cursor);
            }
            cursor.close();
            return order;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting order: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Get order items
     */
    public List<OrderItem> getOrderItems(int orderId) {
        List<OrderItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT oi.*, m." + MENU_NAME + " " +
                    "FROM " + TABLE_ORDER_ITEMS + " oi " +
                    "INNER JOIN " + TABLE_MENU + " m ON oi." + ITEM_MENU_ID + " = m." + MENU_ID + " " +
                    "WHERE oi." + ITEM_ORDER_ID + " = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(orderId)});

            if (cursor.moveToFirst()) {
                do {
                    OrderItem item = new OrderItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_ID)));
                    item.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_ORDER_ID)));
                    item.setMenuId(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_MENU_ID)));
                    item.setQty(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_QTY)));
                    item.setPrice(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_PRICE)));
                    item.setSubtotal(cursor.getInt(cursor.getColumnIndexOrThrow(ITEM_SUBTOTAL)));
                    item.setMenuName(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    items.add(item);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting order items: " + e.getMessage(), e);
        }

        return items;
    }

    /**
     * Helper: Map cursor to Order object
     */
    private Order mapCursorToOrder(Cursor cursor) {
        Order order = new Order();
        order.setId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_ID)));
        order.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_BUYER_ID)));
        order.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_STAND_ID)));
        order.setTotal(cursor.getInt(cursor.getColumnIndexOrThrow(ORDER_TOTAL)));
        order.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_STATUS)));
        order.setPaymentMethod(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_PAYMENT_METHOD)));
        order.setPaymentStatus(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_PAYMENT_STATUS)));
        order.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_BUYER_NOTES)));
        order.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_CREATED_AT)));
        order.setUpdatedAt(cursor.getString(cursor.getColumnIndexOrThrow(ORDER_UPDATED_AT)));
        order.setStandName(cursor.getString(cursor.getColumnIndexOrThrow(STAND_NAME)));
        order.setUserName(cursor.getString(cursor.getColumnIndexOrThrow("buyer_name")));
        return order;
    }

    // ==================== NOTIFICATION SYSTEM ====================

    /**
     * Create notification
     */
    public long createNotification(int userId, String type, String title,
                                   String message, int orderId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(NOTIF_USER_ID, userId);
            values.put(NOTIF_TYPE, type);
            values.put(NOTIF_TITLE, title);
            values.put(NOTIF_MESSAGE, message);
            values.put(NOTIF_ORDER_ID, orderId);
            values.put(NOTIF_IS_READ, 0);
            values.put(NOTIF_CREATED_AT, getCurrentTimestamp());

            return db.insert(TABLE_NOTIFICATIONS, null, values);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error creating notification: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Get unread notifications
     */
    public List<Notification> getUnreadNotifications(int userId) {
        List<Notification> notifications = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT * FROM " + TABLE_NOTIFICATIONS +
                    " WHERE " + NOTIF_USER_ID + " = ? AND " + NOTIF_IS_READ + " = 0 " +
                    "ORDER BY " + NOTIF_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    Notification notif = new Notification();
                    notif.setId(cursor.getInt(cursor.getColumnIndexOrThrow(NOTIF_ID)));
                    notif.setUserId(cursor.getInt(cursor.getColumnIndexOrThrow(NOTIF_USER_ID)));
                    notif.setType(cursor.getString(cursor.getColumnIndexOrThrow(NOTIF_TYPE)));
                    notif.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(NOTIF_TITLE)));
                    notif.setMessage(cursor.getString(cursor.getColumnIndexOrThrow(NOTIF_MESSAGE)));
                    notif.setOrderId(cursor.getInt(cursor.getColumnIndexOrThrow(NOTIF_ORDER_ID)));
                    notif.setRead(cursor.getInt(cursor.getColumnIndexOrThrow(NOTIF_IS_READ)) == 1);
                    notif.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(NOTIF_CREATED_AT)));
                    notifications.add(notif);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting notifications: " + e.getMessage(), e);
        }

        return notifications;
    }

    /**
     * Mark notification as read
     */
    public int markNotificationRead(int notificationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            ContentValues values = new ContentValues();
            values.put(NOTIF_IS_READ, 1);

            return db.update(TABLE_NOTIFICATIONS, values, NOTIF_ID + " = ?",
                    new String[]{String.valueOf(notificationId)});

        } catch (Exception e) {
            Log.e(TAG, "❌ Error marking notification: " + e.getMessage(), e);
            return 0;
        }
    }

    /**
     * Get unread notification count
     */
    public int getUnreadNotificationCount(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            Cursor cursor = db.rawQuery(
                    "SELECT COUNT(*) FROM " + TABLE_NOTIFICATIONS +
                            " WHERE " + NOTIF_USER_ID + " = ? AND " + NOTIF_IS_READ + " = 0",
                    new String[]{String.valueOf(userId)});

            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting notification count: " + e.getMessage(), e);
            return 0;
        }
    }

    // ==================== FAVORITES & REVIEWS (SAME AS BEFORE) ====================

    public long addToFavorites(int buyerId, int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(FAV_BUYER_ID, buyerId);
            values.put(FAV_MENU_ID, menuId);
            values.put(FAV_CREATED_AT, getCurrentTimestamp());
            return db.insert(TABLE_FAVORITES, null, values);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding favorite: " + e.getMessage(), e);
            return -1;
        }
    }

    public int removeFromFavorites(int buyerId, int menuId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            return db.delete(TABLE_FAVORITES,
                    FAV_BUYER_ID + " = ? AND " + FAV_MENU_ID + " = ?",
                    new String[]{String.valueOf(buyerId), String.valueOf(menuId)});
        } catch (Exception e) {
            Log.e(TAG, "❌ Error removing favorite: " + e.getMessage(), e);
            return 0;
        }
    }

    public boolean isFavorite(int buyerId, int menuId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery(
                    "SELECT * FROM " + TABLE_FAVORITES +
                            " WHERE " + FAV_BUYER_ID + " = ? AND " + FAV_MENU_ID + " = ?",
                    new String[]{String.valueOf(buyerId), String.valueOf(menuId)});
            boolean exists = cursor.getCount() > 0;
            cursor.close();
            return exists;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error checking favorite: " + e.getMessage(), e);
            return false;
        }
    }

    public long addReview(int buyerId, int menuId, int orderId, int rating, String comment) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(REVIEW_BUYER_ID, buyerId);
            values.put(REVIEW_MENU_ID, menuId);
            values.put(REVIEW_ORDER_ID, orderId);
            values.put(REVIEW_RATING, rating);
            values.put(REVIEW_COMMENT, comment);
            values.put(REVIEW_CREATED_AT, getCurrentTimestamp());
            return db.insert(TABLE_REVIEWS, null, values);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error adding review: " + e.getMessage(), e);
            return -1;
        }
    }

    // ==================== HELPER METHODS ====================

    public Stand getStandById(int standId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_STAND + " WHERE " + STAND_ID + " = ?",
                    new String[]{String.valueOf(standId)});

            Stand stand = null;
            if (cursor.moveToFirst()) {
                stand = new Stand();
                stand.setId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_ID)));
                stand.setOwnerId(cursor.getInt(cursor.getColumnIndexOrThrow(STAND_SELLER_ID)));
                stand.setNama(cursor.getString(cursor.getColumnIndexOrThrow(STAND_NAME)));
                stand.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(STAND_DESCRIPTION)));
                stand.setImage(cursor.getString(cursor.getColumnIndexOrThrow(STAND_IMAGE)));
            }
            cursor.close();
            return stand;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting stand by ID: " + e.getMessage(), e);
            return null;
        }
    }

    // ==================== STATISTICS (FOR DASHBOARD) ====================

    public int getTotalOrdersByStatus(int sellerId, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "WHERE s." + STAND_SELLER_ID + " = ? AND o." + ORDER_STATUS + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId), status});
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting order count: " + e.getMessage(), e);
            return 0;
        }
    }

    public int getTotalRevenue(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query = "SELECT SUM(o." + ORDER_TOTAL + ") FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "WHERE s." + STAND_SELLER_ID + " = ? AND o." + ORDER_STATUS + " = 'completed'";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId)});
            int total = 0;
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            cursor.close();
            return total;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting revenue: " + e.getMessage(), e);
            return 0;
        }
    }

    public int getTodayRevenue(int sellerId) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

            String query = "SELECT SUM(o." + ORDER_TOTAL + ") FROM " + TABLE_ORDERS + " o " +
                    "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                    "WHERE s." + STAND_SELLER_ID + " = ? " +
                    "AND o." + ORDER_STATUS + " = 'completed' " +
                    "AND DATE(o." + ORDER_CREATED_AT + ") = ?";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(sellerId), today});
            int total = 0;
            if (cursor.moveToFirst()) {
                total = cursor.getInt(0);
            }
            cursor.close();
            return total;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting today revenue: " + e.getMessage(), e);
            return 0;
        }
    }


    public int getTotalOrdersBySeller(int sellerId, String status) {
        SQLiteDatabase db = this.getReadableDatabase();
        try {
            String query;
            String[] args;

            if ("all".equals(status)) {
                query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " o " +
                        "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                        "WHERE s." + STAND_SELLER_ID + " = ?";
                args = new String[]{String.valueOf(sellerId)};
            } else {
                query = "SELECT COUNT(*) FROM " + TABLE_ORDERS + " o " +
                        "INNER JOIN " + TABLE_STAND + " s ON o." + ORDER_STAND_ID + " = s." + STAND_ID + " " +
                        "WHERE s." + STAND_SELLER_ID + " = ? AND o." + ORDER_STATUS + " = ?";
                args = new String[]{String.valueOf(sellerId), status};
            }

            Cursor cursor = db.rawQuery(query, args);
            int count = 0;
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
            return count;
        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting orders: " + e.getMessage(), e);
            return 0;
        }
    }


    public List<Menu> getFavoriteMenus(int buyerId) {
        List<Menu> menus = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT m.*, " +
                    "COALESCE(AVG(r." + REVIEW_RATING + "), 0) as avg_rating, " +
                    "COUNT(DISTINCT r." + REVIEW_ID + ") as total_reviews " +
                    "FROM " + TABLE_FAVORITES + " f " +
                    "INNER JOIN " + TABLE_MENU + " m ON f." + FAV_MENU_ID + " = m." + MENU_ID + " " +
                    "LEFT JOIN " + TABLE_REVIEWS + " r ON m." + MENU_ID + " = r." + REVIEW_MENU_ID + " " +
                    "WHERE f." + FAV_BUYER_ID + " = ? " +
                    "GROUP BY m." + MENU_ID + " " +
                    "ORDER BY f." + FAV_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

            if (cursor.moveToFirst()) {
                do {
                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_STAND_ID)));
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setImage(cursor.getString(cursor.getColumnIndexOrThrow(MENU_IMAGE)));
                    menu.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(MENU_DESCRIPTION)));
                    menu.setKategori(cursor.getString(cursor.getColumnIndexOrThrow(MENU_CATEGORY)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));
                    menu.setAverageRating(cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating")));
                    menu.setTotalReviews(cursor.getInt(cursor.getColumnIndexOrThrow("total_reviews")));
                    menus.add(menu);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting favorite menus: " + e.getMessage(), e);
        }

        return menus;
    }


    public List<Review> getMenuReviews(int menuId) {
        List<Review> reviews = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT r.*, u." + USER_NAME + " " +
                    "FROM " + TABLE_REVIEWS + " r " +
                    "INNER JOIN " + TABLE_USERS + " u ON r." + REVIEW_BUYER_ID + " = u." + USER_ID + " " +
                    "WHERE r." + REVIEW_MENU_ID + " = ? " +
                    "ORDER BY r." + REVIEW_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(menuId)});

            if (cursor.moveToFirst()) {
                do {
                    Review review = new Review();
                    review.setId(cursor.getInt(cursor.getColumnIndexOrThrow(REVIEW_ID)));
                    review.setBuyerId(cursor.getInt(cursor.getColumnIndexOrThrow(REVIEW_BUYER_ID)));
                    review.setMenuId(cursor.getInt(cursor.getColumnIndexOrThrow(REVIEW_MENU_ID)));
                    review.setRating(cursor.getInt(cursor.getColumnIndexOrThrow(REVIEW_RATING)));
                    review.setComment(cursor.getString(cursor.getColumnIndexOrThrow(REVIEW_COMMENT)));
                    review.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(REVIEW_CREATED_AT)));
                    review.setBuyerName(cursor.getString(cursor.getColumnIndexOrThrow(USER_NAME)));
                    reviews.add(review);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting reviews: " + e.getMessage(), e);
        }

        return reviews;
    }


    public List<Menu> searchMenus(String query) {
        List<Menu> menus = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String sql = "SELECT m.*, " +
                    "COALESCE(AVG(r." + REVIEW_RATING + "), 0) as avg_rating, " +
                    "COUNT(DISTINCT r." + REVIEW_ID + ") as total_reviews " +
                    "FROM " + TABLE_MENU + " m " +
                    "LEFT JOIN " + TABLE_REVIEWS + " r ON m." + MENU_ID + " = r." + REVIEW_MENU_ID + " " +
                    "WHERE m." + MENU_NAME + " LIKE ? " +
                    "OR m." + MENU_DESCRIPTION + " LIKE ? " +
                    "OR m." + MENU_CATEGORY + " LIKE ? " +
                    "GROUP BY m." + MENU_ID + " " +
                    "ORDER BY m." + MENU_NAME;

            String searchPattern = "%" + query + "%";
            Cursor cursor = db.rawQuery(sql, new String[]{searchPattern, searchPattern, searchPattern});

            if (cursor.moveToFirst()) {
                do {
                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_STAND_ID)));
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setImage(cursor.getString(cursor.getColumnIndexOrThrow(MENU_IMAGE)));
                    menu.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(MENU_DESCRIPTION)));
                    menu.setKategori(cursor.getString(cursor.getColumnIndexOrThrow(MENU_CATEGORY)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));
                    menu.setAverageRating(cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating")));
                    menu.setTotalReviews(cursor.getInt(cursor.getColumnIndexOrThrow("total_reviews")));
                    menus.add(menu);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error searching menus: " + e.getMessage(), e);
        }

        return menus;
    }



    public Map<Integer, List<CartItem>> getCartItemsGroupedByStand(int buyerId) {
        Map<Integer, List<CartItem>> groupedItems = new LinkedHashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT c.*, m.*, s." + STAND_ID + " " +
                    "FROM " + TABLE_CART + " c " +
                    "INNER JOIN " + TABLE_MENU + " m ON c." + CART_MENU_ID + " = m." + MENU_ID + " " +
                    "INNER JOIN " + TABLE_STAND + " s ON m." + MENU_STAND_ID + " = s." + STAND_ID + " " +
                    "WHERE c." + CART_BUYER_ID + " = ? " +
                    "ORDER BY s." + STAND_NAME + ", m." + MENU_NAME;

            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(buyerId)});

            if (cursor.moveToFirst()) {
                do {
                    int standId = cursor.getInt(cursor.getColumnIndexOrThrow(STAND_ID));

                    CartItem item = new CartItem();
                    item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(CART_ID)));
                    item.setUserId(buyerId);
                    item.setQty(cursor.getInt(cursor.getColumnIndexOrThrow(CART_QTY)));
                    item.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(CART_NOTES)));

                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(standId);
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));

                    item.setMenu(menu);

                    // Group by stand
                    if (!groupedItems.containsKey(standId)) {
                        groupedItems.put(standId, new ArrayList<>());
                    }
                    groupedItems.get(standId).add(item);

                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting grouped cart items: " + e.getMessage(), e);
        }

        return groupedItems;
    }


    public List<Menu> getAllAvailableMenus() {
        List<Menu> menus = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            String query = "SELECT m.*, " +
                    "COALESCE(AVG(r." + REVIEW_RATING + "), 0) as avg_rating, " +
                    "COUNT(DISTINCT r." + REVIEW_ID + ") as total_reviews " +
                    "FROM " + TABLE_MENU + " m " +
                    "LEFT JOIN " + TABLE_REVIEWS + " r ON m." + MENU_ID + " = r." + REVIEW_MENU_ID + " " +
                    "WHERE m." + MENU_STATUS + " = 'available' " +
                    "GROUP BY m." + MENU_ID + " " +
                    "ORDER BY m." + MENU_CREATED_AT + " DESC";

            Cursor cursor = db.rawQuery(query, null);

            if (cursor.moveToFirst()) {
                do {
                    Menu menu = new Menu();
                    menu.setId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_ID)));
                    menu.setStandId(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_STAND_ID)));
                    menu.setNama(cursor.getString(cursor.getColumnIndexOrThrow(MENU_NAME)));
                    menu.setHarga(cursor.getInt(cursor.getColumnIndexOrThrow(MENU_PRICE)));
                    menu.setImage(cursor.getString(cursor.getColumnIndexOrThrow(MENU_IMAGE)));
                    menu.setDeskripsi(cursor.getString(cursor.getColumnIndexOrThrow(MENU_DESCRIPTION)));
                    menu.setKategori(cursor.getString(cursor.getColumnIndexOrThrow(MENU_CATEGORY)));
                    menu.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(MENU_STATUS)));
                    menu.setAverageRating(cursor.getFloat(cursor.getColumnIndexOrThrow("avg_rating")));
                    menu.setTotalReviews(cursor.getInt(cursor.getColumnIndexOrThrow("total_reviews")));
                    menus.add(menu);
                } while (cursor.moveToNext());
            }
            cursor.close();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error getting available menus: " + e.getMessage(), e);
        }

        return menus;
    }



}

// ==================== MODEL CLASSES (Add these as inner classes or separate files) ====================

