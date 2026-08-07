-- SQLite Database Schema for Sugarcane ERP

CREATE TABLE IF NOT EXISTS Users (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    username TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    pin_hash TEXT,
    role TEXT NOT NULL DEFAULT 'ADMIN',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert default admin if not exists (password: admin, pin: 1234)
INSERT OR IGNORE INTO Users (username, password_hash, pin_hash, role) 
VALUES ('admin', 'admin', '1234', 'ADMIN');

CREATE TABLE IF NOT EXISTS Settings (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    setting_key TEXT UNIQUE NOT NULL,
    setting_value TEXT,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Default Settings
INSERT OR IGNORE INTO Settings (setting_key, setting_value) VALUES ('COMPANY_NAME', 'Sugarcane Supplier');
INSERT OR IGNORE INTO Settings (setting_key, setting_value) VALUES ('LANGUAGE', 'en');
INSERT OR IGNORE INTO Settings (setting_key, setting_value) VALUES ('THEME', 'light');

CREATE TABLE IF NOT EXISTS Farmers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    mobile TEXT,
    village TEXT,
    taluka TEXT,
    district TEXT,
    address TEXT,
    aadhar_number TEXT,
    bank_details TEXT,
    opening_balance REAL DEFAULT 0.0,
    remarks TEXT,
    status TEXT DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Customers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    mobile TEXT,
    village TEXT,
    address TEXT,
    gst TEXT,
    opening_balance REAL DEFAULT 0.0,
    status TEXT DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Workers (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    mobile TEXT,
    village TEXT,
    work_type TEXT,
    joining_date DATE,
    status TEXT DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Transports (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transport_name TEXT,
    vehicle_no TEXT NOT NULL,
    driver_name TEXT,
    driver_mobile TEXT,
    status TEXT DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS Sugarcane_Purchases (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id INTEGER NOT NULL,
    bill_no TEXT,
    purchase_date DATE NOT NULL,
    cane_type TEXT,
    vehicle_no TEXT,
    empty_weight REAL DEFAULT 0.0,
    loaded_weight REAL DEFAULT 0.0,
    weight REAL NOT NULL,
    rate_per_ton REAL NOT NULL,
    total_amount REAL NOT NULL,
    advance REAL DEFAULT 0.0,
    loading_charges REAL DEFAULT 0.0,
    cutting_charges REAL DEFAULT 0.0,
    transport_charges REAL DEFAULT 0.0,
    other_charges REAL DEFAULT 0.0,
    net_amount REAL NOT NULL,
    remarks TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(farmer_id) REFERENCES Farmers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Farmer_Payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    farmer_id INTEGER NOT NULL,
    payment_date DATE NOT NULL,
    amount REAL NOT NULL,
    payment_mode TEXT,
    ref_no TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(farmer_id) REFERENCES Farmers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Sugarcane_Sales (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    sale_date DATE NOT NULL,
    cane_type TEXT,
    vehicle_no TEXT,
    weight REAL DEFAULT 0.0,
    rate_per_ton REAL NOT NULL,
    total_amount REAL NOT NULL,
    received_amount REAL DEFAULT 0.0,
    net_amount REAL NOT NULL,
    remarks TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(customer_id) REFERENCES Customers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Customer_Collections (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    customer_id INTEGER NOT NULL,
    collection_date DATE NOT NULL,
    amount REAL NOT NULL,
    payment_mode TEXT,
    ref_no TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(customer_id) REFERENCES Customers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Worker_Daily_Entries (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    worker_id INTEGER NOT NULL,
    entry_date DATE NOT NULL,
    attendance TEXT NOT NULL, -- PRESENT, ABSENT, HALF_DAY
    bundles INTEGER DEFAULT 0,
    rate_per_bundle REAL DEFAULT 0.0,
    total_salary REAL DEFAULT 0.0,
    bonus REAL DEFAULT 0.0,
    advance REAL DEFAULT 0.0,
    penalty REAL DEFAULT 0.0,
    tea_expense REAL DEFAULT 0.0,
    food_expense REAL DEFAULT 0.0,
    other_expense REAL DEFAULT 0.0,
    net_salary REAL DEFAULT 0.0,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(worker_id) REFERENCES Workers(id) ON DELETE RESTRICT
);

CREATE TABLE IF NOT EXISTS Transport_Trips (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    transport_id INTEGER NOT NULL,
    trip_date DATE NOT NULL,
    farmer_id INTEGER,
    customer_id INTEGER,
    pickup_location TEXT,
    destination TEXT,
    weight REAL DEFAULT 0.0,
    trip_charge REAL DEFAULT 0.0,
    diesel REAL DEFAULT 0.0,
    toll REAL DEFAULT 0.0,
    advance REAL DEFAULT 0.0,
    balance REAL DEFAULT 0.0,
    trip_status TEXT DEFAULT 'COMPLETED',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(transport_id) REFERENCES Transports(id) ON DELETE RESTRICT,
    FOREIGN KEY(farmer_id) REFERENCES Farmers(id) ON DELETE SET NULL,
    FOREIGN KEY(customer_id) REFERENCES Customers(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS Expenses (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    expense_date DATE NOT NULL,
    category TEXT NOT NULL, -- Diesel, Tea, Food, Office, Repair, etc.
    amount REAL NOT NULL,
    description TEXT,
    payment_mode TEXT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
);
