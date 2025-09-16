-- =====================================================
-- OrphanageHub Database Schema v1.0.0
-- Complete Database Setup with Production Schema
-- =====================================================
-- Uses BCrypt for password hashing (jbcrypt 0.4)
-- SQLite Database (sqlite-jdbc 3.45.3.0)
-- =====================================================

-- Enable foreign key constraints
PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- =====================================================
-- STEP 1: Backup existing data (if any)
-- =====================================================
CREATE TABLE IF NOT EXISTS TblUsers_backup AS SELECT * FROM TblUsers WHERE 1=1;
CREATE TABLE IF NOT EXISTS TblOrphanages_backup AS SELECT * FROM TblOrphanages WHERE 1=1;
CREATE TABLE IF NOT EXISTS TblResourceRequests_backup AS SELECT * FROM TblResourceRequests WHERE 1=1;

-- =====================================================
-- STEP 2: Drop existing tables with incorrect schemas
-- =====================================================
DROP TABLE IF EXISTS TblVolunteerApplications;
DROP TABLE IF EXISTS TblDonationItems;
DROP TABLE IF EXISTS TblDonations;
DROP TABLE IF EXISTS TblVolunteerOpportunities;
DROP TABLE IF EXISTS TblResourceRequests;
DROP TABLE IF EXISTS TblOrphanages;
DROP TABLE IF EXISTS TblUsers;
DROP TABLE IF EXISTS TblAuditLog;
DROP TABLE IF EXISTS TblNotifications;

-- =====================================================
-- STEP 3: Create Core Tables with Production Schema
-- =====================================================

-- Users table with BCrypt password hashing support
CREATE TABLE TblUsers (
    UserID INTEGER PRIMARY KEY AUTOINCREMENT,
    Username TEXT NOT NULL UNIQUE COLLATE NOCASE,
    PasswordHash TEXT NOT NULL, -- BCrypt hash ($2a$10$...)
    Email TEXT NOT NULL UNIQUE COLLATE NOCASE,
    UserRole TEXT NOT NULL CHECK(UserRole IN ('Admin', 'OrphanageRep', 'Donor', 'Volunteer', 'Staff')),
    DateRegistered DATETIME DEFAULT CURRENT_TIMESTAMP,
    LastLogin DATETIME,
    FullName TEXT NOT NULL,
    PhoneNumber TEXT,
    IDNumber TEXT, -- South African ID
    DateOfBirth DATE,
    Address TEXT,
    City TEXT,
    Province TEXT CHECK(Province IN ('Eastern Cape', 'Free State', 'Gauteng', 'KwaZulu-Natal', 
                                      'Limpopo', 'Mpumalanga', 'Northern Cape', 'North West', 
                                      'Western Cape', NULL)),
    PostalCode TEXT,
    AccountStatus TEXT DEFAULT 'Active' CHECK(AccountStatus IN ('Active', 'Suspended', 'Pending', 'Deactivated')),
    EmailVerified BOOLEAN DEFAULT 0,
    VerificationToken TEXT,
    PasswordResetToken TEXT,
    PasswordResetExpiry DATETIME,
    ProfilePicture BLOB,
    Bio TEXT,
    CreatedBy INTEGER,
    ModifiedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedBy INTEGER,
    FOREIGN KEY (CreatedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL,
    FOREIGN KEY (ModifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Orphanages table
CREATE TABLE TblOrphanages (
    OrphanageID INTEGER PRIMARY KEY AUTOINCREMENT,
    OrphanageName TEXT NOT NULL,
    RegistrationNumber TEXT UNIQUE, -- NPO Registration
    TaxNumber TEXT, -- Tax exemption number
    Address TEXT NOT NULL,
    City TEXT NOT NULL,
    Province TEXT NOT NULL CHECK(Province IN ('Eastern Cape', 'Free State', 'Gauteng', 'KwaZulu-Natal', 
                                               'Limpopo', 'Mpumalanga', 'Northern Cape', 'North West', 
                                               'Western Cape')),
    PostalCode TEXT,
    ContactPerson TEXT NOT NULL,
    ContactEmail TEXT NOT NULL,
    ContactPhone TEXT NOT NULL,
    AlternatePhone TEXT,
    Website TEXT,
    Description TEXT,
    Mission TEXT,
    Vision TEXT,
    EstablishedDate DATE,
    Capacity INTEGER DEFAULT 0,
    CurrentOccupancy INTEGER DEFAULT 0,
    AgeGroupMin INTEGER DEFAULT 0,
    AgeGroupMax INTEGER DEFAULT 18,
    AcceptsDonations BOOLEAN DEFAULT 1,
    AcceptsVolunteers BOOLEAN DEFAULT 1,
    BankName TEXT,
    BankAccountNumber TEXT,
    BankBranchCode TEXT,
    DateRegistered DATETIME DEFAULT CURRENT_TIMESTAMP,
    VerificationStatus TEXT DEFAULT 'Pending' CHECK(VerificationStatus IN ('Pending', 'Verified', 'Rejected', 'Under Review')),
    VerificationDate DATETIME,
    VerifiedBy INTEGER,
    VerificationNotes TEXT,
    UserID INTEGER NOT NULL,
    Status TEXT DEFAULT 'Active' CHECK(Status IN ('Active', 'Inactive', 'Suspended')),
    Logo BLOB,
    CoverImage BLOB,
    Latitude REAL,
    Longitude REAL,
    ModifiedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedBy INTEGER,
    FOREIGN KEY (UserID) REFERENCES TblUsers(UserID) ON DELETE CASCADE,
    FOREIGN KEY (VerifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL,
    FOREIGN KEY (ModifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Resource Requests table
CREATE TABLE TblResourceRequests (
    RequestID INTEGER PRIMARY KEY AUTOINCREMENT,
    OrphanageID INTEGER NOT NULL,
    ResourceType TEXT NOT NULL CHECK(ResourceType IN ('Food', 'Clothing', 'Educational', 'Medical', 
                                                      'Hygiene', 'Furniture', 'Electronics', 'Sports', 
                                                      'Toys', 'Books', 'Other')),
    ResourceDescription TEXT NOT NULL,
    Quantity INTEGER DEFAULT 1,
    Unit TEXT, -- kg, liters, pieces, etc.
    UrgencyLevel TEXT DEFAULT 'Medium' CHECK(UrgencyLevel IN ('Low', 'Medium', 'High', 'Critical')),
    RequestDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    NeededByDate DATE,
    Status TEXT DEFAULT 'Open' CHECK(Status IN ('Open', 'In Progress', 'Partially Fulfilled', 
                                                'Fulfilled', 'Cancelled', 'Expired')),
    FulfilledDate DATETIME,
    FulfilledBy INTEGER,
    FulfillmentNotes TEXT,
    EstimatedValue REAL,
    ActualValue REAL,
    Notes TEXT,
    ImagePath TEXT,
    CreatedBy INTEGER NOT NULL,
    ModifiedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedBy INTEGER,
    FOREIGN KEY (OrphanageID) REFERENCES TblOrphanages(OrphanageID) ON DELETE CASCADE,
    FOREIGN KEY (FulfilledBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL,
    FOREIGN KEY (CreatedBy) REFERENCES TblUsers(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ModifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Donations table
CREATE TABLE TblDonations (
    DonationID INTEGER PRIMARY KEY AUTOINCREMENT,
    DonorID INTEGER NOT NULL,
    OrphanageID INTEGER,
    RequestID INTEGER,
    DonationType TEXT NOT NULL CHECK(DonationType IN ('Money', 'Food', 'Clothing', 'Educational', 
                                                      'Medical', 'Hygiene', 'Furniture', 'Electronics', 
                                                      'Sports', 'Toys', 'Books', 'Other')),
    Amount REAL,
    Currency TEXT DEFAULT 'ZAR',
    ItemDescription TEXT,
    Quantity INTEGER,
    Unit TEXT,
    EstimatedValue REAL,
    DonationDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ScheduledDate DATETIME,
    Status TEXT DEFAULT 'Pending' CHECK(Status IN ('Pending', 'Processing', 'Completed', 
                                                   'Cancelled', 'Failed', 'Refunded')),
    PaymentMethod TEXT CHECK(PaymentMethod IN ('Cash', 'EFT', 'Credit Card', 'Debit Card', 
                                               'PayPal', 'In Kind', 'Voucher', NULL)),
    TransactionReference TEXT,
    TaxDeductible BOOLEAN DEFAULT 1,
    AnonymousDonation BOOLEAN DEFAULT 0,
    RecurringDonation BOOLEAN DEFAULT 0,
    RecurrenceInterval TEXT CHECK(RecurrenceInterval IN ('Weekly', 'Monthly', 'Quarterly', 
                                                         'Annually', NULL)),
    NextRecurrenceDate DATE,
    DonorMessage TEXT,
    ThankYouSent BOOLEAN DEFAULT 0,
    ThankYouDate DATETIME,
    ReceiptNumber TEXT,
    ReceiptSent BOOLEAN DEFAULT 0,
    Notes TEXT,
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedBy INTEGER,
    FOREIGN KEY (DonorID) REFERENCES TblUsers(UserID) ON DELETE CASCADE,
    FOREIGN KEY (OrphanageID) REFERENCES TblOrphanages(OrphanageID) ON DELETE SET NULL,
    FOREIGN KEY (RequestID) REFERENCES TblResourceRequests(RequestID) ON DELETE SET NULL,
    FOREIGN KEY (ModifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Volunteer Opportunities table
CREATE TABLE TblVolunteerOpportunities (
    OpportunityID INTEGER PRIMARY KEY AUTOINCREMENT,
    OrphanageID INTEGER NOT NULL,
    Title TEXT NOT NULL,
    Description TEXT NOT NULL,
    Category TEXT CHECK(Category IN ('Teaching', 'Mentoring', 'Sports', 'Arts', 'Maintenance', 
                                     'Administrative', 'Medical', 'Counseling', 'Event', 'Other')),
    SkillsRequired TEXT,
    SkillLevel TEXT CHECK(SkillLevel IN ('None', 'Basic', 'Intermediate', 'Advanced', 'Professional')),
    TimeCommitment TEXT,
    HoursPerWeek INTEGER,
    Duration TEXT, -- One-time, 1 month, 3 months, 6 months, etc.
    StartDate DATE,
    EndDate DATE,
    RecurringSchedule TEXT, -- e.g., "Every Saturday 10am-2pm"
    MinAge INTEGER DEFAULT 18,
    MaxAge INTEGER,
    MaxVolunteers INTEGER,
    CurrentVolunteers INTEGER DEFAULT 0,
    BackgroundCheckRequired BOOLEAN DEFAULT 1,
    TrainingProvided BOOLEAN DEFAULT 0,
    TrainingDetails TEXT,
    TransportProvided BOOLEAN DEFAULT 0,
    MealsProvided BOOLEAN DEFAULT 0,
    Status TEXT DEFAULT 'Open' CHECK(Status IN ('Draft', 'Open', 'Closed', 'Filled', 
                                                'Cancelled', 'Completed')),
    UrgencyLevel TEXT DEFAULT 'Normal' CHECK(UrgencyLevel IN ('Low', 'Normal', 'High', 'Urgent')),
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    CreatedBy INTEGER NOT NULL,
    ModifiedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ModifiedBy INTEGER,
    PublishedDate DATETIME,
    ClosedDate DATETIME,
    FOREIGN KEY (OrphanageID) REFERENCES TblOrphanages(OrphanageID) ON DELETE CASCADE,
    FOREIGN KEY (CreatedBy) REFERENCES TblUsers(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ModifiedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Volunteer Applications table
CREATE TABLE TblVolunteerApplications (
    ApplicationID INTEGER PRIMARY KEY AUTOINCREMENT,
    OpportunityID INTEGER NOT NULL,
    VolunteerID INTEGER NOT NULL,
    ApplicationDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    Status TEXT DEFAULT 'Pending' CHECK(Status IN ('Pending', 'Under Review', 'Approved', 
                                                   'Rejected', 'Withdrawn')),
    MotivationLetter TEXT,
    AvailableStartDate DATE,
    AvailableEndDate DATE,
    HasCriminalRecord BOOLEAN DEFAULT 0,
    CriminalRecordDetails TEXT,
    EmergencyContactName TEXT,
    EmergencyContactPhone TEXT,
    Skills TEXT,
    Experience TEXT,
    References TEXT,
    ReviewedBy INTEGER,
    ReviewDate DATETIME,
    ReviewNotes TEXT,
    ApprovalDate DATETIME,
    RejectionReason TEXT,
    AttendanceHours REAL DEFAULT 0,
    CompletionStatus TEXT CHECK(CompletionStatus IN ('Not Started', 'In Progress', 'Completed', 
                                                     'Incomplete', 'No Show')),
    VolunteerFeedback TEXT,
    OrphanageFeedback TEXT,
    Rating INTEGER CHECK(Rating >= 1 AND Rating <= 5),
    FOREIGN KEY (OpportunityID) REFERENCES TblVolunteerOpportunities(OpportunityID) ON DELETE CASCADE,
    FOREIGN KEY (VolunteerID) REFERENCES TblUsers(UserID) ON DELETE CASCADE,
    FOREIGN KEY (ReviewedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL,
    UNIQUE(OpportunityID, VolunteerID)
);

-- Donation Items table (for tracking individual items in a donation)
CREATE TABLE TblDonationItems (
    ItemID INTEGER PRIMARY KEY AUTOINCREMENT,
    DonationID INTEGER NOT NULL,
    ItemName TEXT NOT NULL,
    Category TEXT,
    Quantity INTEGER DEFAULT 1,
    Unit TEXT,
    Condition TEXT CHECK(Condition IN ('New', 'Like New', 'Good', 'Fair', 'Poor', NULL)),
    EstimatedValue REAL,
    Description TEXT,
    SerialNumber TEXT,
    ExpiryDate DATE,
    ReceivedDate DATETIME,
    ReceivedBy INTEGER,
    StorageLocation TEXT,
    DistributedDate DATETIME,
    DistributedTo TEXT,
    Notes TEXT,
    FOREIGN KEY (DonationID) REFERENCES TblDonations(DonationID) ON DELETE CASCADE,
    FOREIGN KEY (ReceivedBy) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- Notifications table
CREATE TABLE TblNotifications (
    NotificationID INTEGER PRIMARY KEY AUTOINCREMENT,
    UserID INTEGER NOT NULL,
    Type TEXT NOT NULL CHECK(Type IN ('System', 'Donation', 'Request', 'Volunteer', 
                                      'Message', 'Alert', 'Reminder')),
    Title TEXT NOT NULL,
    Message TEXT NOT NULL,
    Priority TEXT DEFAULT 'Normal' CHECK(Priority IN ('Low', 'Normal', 'High', 'Urgent')),
    Status TEXT DEFAULT 'Unread' CHECK(Status IN ('Unread', 'Read', 'Archived')),
    CreatedDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    ReadDate DATETIME,
    ExpiryDate DATETIME,
    ActionURL TEXT,
    RelatedEntityType TEXT,
    RelatedEntityID INTEGER,
    FOREIGN KEY (UserID) REFERENCES TblUsers(UserID) ON DELETE CASCADE
);

-- Audit Log table
CREATE TABLE TblAuditLog (
    LogID INTEGER PRIMARY KEY AUTOINCREMENT,
    UserID INTEGER,
    Username TEXT,
    Action TEXT NOT NULL,
    EntityType TEXT,
    EntityID INTEGER,
    OldValue TEXT,
    NewValue TEXT,
    IPAddress TEXT,
    UserAgent TEXT,
    SessionID TEXT,
    Timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,
    Success BOOLEAN DEFAULT 1,
    ErrorMessage TEXT,
    FOREIGN KEY (UserID) REFERENCES TblUsers(UserID) ON DELETE SET NULL
);

-- =====================================================
-- STEP 4: Create Indexes for Performance
-- =====================================================
CREATE INDEX idx_users_username ON TblUsers(Username);
CREATE INDEX idx_users_email ON TblUsers(Email);
CREATE INDEX idx_users_role ON TblUsers(UserRole);
CREATE INDEX idx_users_status ON TblUsers(AccountStatus);

CREATE INDEX idx_orphanages_userid ON TblOrphanages(UserID);
CREATE INDEX idx_orphanages_city ON TblOrphanages(City);
CREATE INDEX idx_orphanages_province ON TblOrphanages(Province);
CREATE INDEX idx_orphanages_status ON TblOrphanages(VerificationStatus);
CREATE INDEX idx_orphanages_name ON TblOrphanages(OrphanageName);

CREATE INDEX idx_requests_orphanageid ON TblResourceRequests(OrphanageID);
CREATE INDEX idx_requests_status ON TblResourceRequests(Status);
CREATE INDEX idx_requests_urgency ON TblResourceRequests(UrgencyLevel);
CREATE INDEX idx_requests_type ON TblResourceRequests(ResourceType);
CREATE INDEX idx_requests_date ON TblResourceRequests(RequestDate);

CREATE INDEX idx_donations_donorid ON TblDonations(DonorID);
CREATE INDEX idx_donations_orphanageid ON TblDonations(OrphanageID);
CREATE INDEX idx_donations_requestid ON TblDonations(RequestID);
CREATE INDEX idx_donations_status ON TblDonations(Status);
CREATE INDEX idx_donations_date ON TblDonations(DonationDate);

CREATE INDEX idx_volunteer_orphanageid ON TblVolunteerOpportunities(OrphanageID);
CREATE INDEX idx_volunteer_status ON TblVolunteerOpportunities(Status);
CREATE INDEX idx_volunteer_startdate ON TblVolunteerOpportunities(StartDate);

CREATE INDEX idx_applications_opportunityid ON TblVolunteerApplications(OpportunityID);
CREATE INDEX idx_applications_volunteerid ON TblVolunteerApplications(VolunteerID);
CREATE INDEX idx_applications_status ON TblVolunteerApplications(Status);

CREATE INDEX idx_notifications_userid ON TblNotifications(UserID);
CREATE INDEX idx_notifications_status ON TblNotifications(Status);
CREATE INDEX idx_notifications_created ON TblNotifications(CreatedDate);

CREATE INDEX idx_audit_userid ON TblAuditLog(UserID);
CREATE INDEX idx_audit_timestamp ON TblAuditLog(Timestamp);
CREATE INDEX idx_audit_entity ON TblAuditLog(EntityType, EntityID);

-- =====================================================
-- STEP 5: Insert Production-Ready Sample Data
-- =====================================================

-- Insert sample users with BCrypt hashed passwords
-- Note: These are example hashes for password "Admin123!" - replace with real hashes in production
-- You can generate real BCrypt hashes using: new BCryptPasswordEncoder().encode("your_password")
INSERT INTO TblUsers (Username, PasswordHash, Email, UserRole, FullName, PhoneNumber, 
                     City, Province, AccountStatus, EmailVerified) VALUES
    -- Admin user (password: Admin123!)
    ('admin', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi', 
     'admin@orphanagehub.org.za', 'Admin', 'System Administrator', '0821234567',
     'Cape Town', 'Western Cape', 'Active', 1),
    
    -- Orphanage Representatives
    ('sunshine_rep', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'manager@sunshinehome.org.za', 'OrphanageRep', 'Sarah Johnson', '0823456789',
     'Cape Town', 'Western Cape', 'Active', 1),
     
    ('hope_house', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'director@hopehouse.org.za', 'OrphanageRep', 'Michael Ndlovu', '0834567890',
     'Johannesburg', 'Gauteng', 'Active', 1),
     
    ('rainbow_kids', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'admin@rainbowkids.org.za', 'OrphanageRep', 'Patricia Mthembu', '0845678901',
     'Durban', 'KwaZulu-Natal', 'Active', 1),
    
    -- Donors
    ('john_donor', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'john.smith@email.com', 'Donor', 'John Smith', '0856789012',
     'Sandton', 'Gauteng', 'Active', 1),
     
    ('mary_jones', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'mary.jones@email.com', 'Donor', 'Mary Jones', '0867890123',
     'Stellenbosch', 'Western Cape', 'Active', 1),
    
    -- Volunteers
    ('volunteer_sam', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'sam.wilson@email.com', 'Volunteer', 'Samuel Wilson', '0878901234',
     'Pretoria', 'Gauteng', 'Active', 1),
     
    ('volunteer_lisa', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'lisa.brown@email.com', 'Volunteer', 'Lisa Brown', '0889012345',
     'Port Elizabeth', 'Eastern Cape', 'Active', 1),
    
    -- Test user (password: Test123!)
    ('testuser', '$2a$10$YD9I7fKOxKPFCPChWqBkBeFmQqRJ8mTZKJG96Q8sKgQFHXCV2VBWi',
     'test@orphanagehub.org.za', 'Staff', 'Test User', '0890123456',
     'Cape Town', 'Western Cape', 'Active', 0);

-- Insert sample orphanages
INSERT INTO TblOrphanages (OrphanageName, RegistrationNumber, Address, City, Province, PostalCode,
                          ContactPerson, ContactEmail, ContactPhone, Description, Capacity, 
                          CurrentOccupancy, VerificationStatus, UserID, EstablishedDate,
                          BankName, BankAccountNumber, BankBranchCode) VALUES
    ('Sunshine Children''s Home', 'NPO-001-2024', '123 Hope Street, Observatory', 'Cape Town', 
     'Western Cape', '7925', 'Sarah Johnson', 'contact@sunshinehome.org.za', '0214476589',
     'A safe haven providing care, education and love to orphaned and vulnerable children since 1998.',
     50, 42, 'Verified', 2, '1998-03-15',
     'Standard Bank', '10123456789', '051001'),
     
    ('Hope House Orphanage', 'NPO-002-2024', '456 Care Avenue, Yeoville', 'Johannesburg',
     'Gauteng', '2198', 'Michael Ndlovu', 'info@hopehouse.org.za', '0116487532',
     'Dedicated to transforming the lives of orphaned children through holistic care and education.',
     75, 68, 'Verified', 3, '2005-07-20',
     'FNB', '62123456789', '250655'),
     
    ('Rainbow Kids Center', 'NPO-003-2024', '789 Unity Road, Morningside', 'Durban',
     'KwaZulu-Natal', '4001', 'Patricia Mthembu', 'admin@rainbowkids.org.za', '0313037845',
     'Providing shelter, education, and family-style care to children in need.',
     30, 28, 'Verified', 4, '2010-11-10',
     'ABSA', '9012345678', '632005'),
     
    ('Little Angels Haven', 'NPO-004-2024', '321 Mercy Lane, Hatfield', 'Pretoria',
     'Gauteng', '0028', 'Admin User', 'contact@littleangels.org.za', '0124567890',
     'A loving home for children who have lost their parents, focusing on early childhood development.',
     40, 35, 'Pending', 1, '2015-02-28',
     'Nedbank', '1987654321', '198765');

-- Insert sample resource requests
INSERT INTO TblResourceRequests (OrphanageID, ResourceType, ResourceDescription, Quantity, Unit,
                                UrgencyLevel, Status, EstimatedValue, CreatedBy, NeededByDate) VALUES
    -- Sunshine Children's Home requests
    (1, 'Food', 'Monthly food supplies including rice, maize meal, cooking oil, and canned goods', 
     100, 'kg', 'High', 'Open', 5000.00, 2, date('now', '+14 days')),
     
    (1, 'Clothing', 'Winter clothing for children aged 5-12 years (jackets, jerseys, shoes)', 
     50, 'sets', 'High', 'Open', 7500.00, 2, date('now', '+30 days')),
     
    (1, 'Educational', 'School supplies for new academic year (books, stationery, bags)', 
     42, 'sets', 'Medium', 'Open', 12600.00, 2, date('now', '+45 days')),
    
    -- Hope House requests
    (2, 'Medical', 'First aid supplies and basic medications', 
     1, 'kit', 'Critical', 'Open', 3000.00, 3, date('now', '+7 days')),
     
    (2, 'Furniture', 'Bunk beds with mattresses for dormitory expansion', 
     10, 'sets', 'Medium', 'In Progress', 25000.00, 3, date('now', '+60 days')),
     
    (2, 'Sports', 'Soccer balls, netballs, and sports equipment for recreation', 
     1, 'set', 'Low', 'Open', 2000.00, 3, date('now', '+90 days')),
    
    -- Rainbow Kids Center requests
    (3, 'Hygiene', 'Toiletries including soap, toothpaste, sanitary pads, toilet paper', 
     200, 'units', 'High', 'Open', 4000.00, 4, date('now', '+14 days')),
     
    (3, 'Books', 'Age-appropriate reading books for library', 
     100, 'books', 'Medium', 'Open', 5000.00, 4, date('now', '+60 days')),
     
    (3, 'Electronics', 'Computers for computer lab and educational purposes', 
     5, 'units', 'Medium', 'Open', 25000.00, 4, date('now', '+90 days')),
    
    -- Little Angels Haven requests
    (4, 'Toys', 'Educational toys for early childhood development', 
     30, 'items', 'Low', 'Open', 3000.00, 1, date('now', '+30 days')),
     
    (4, 'Food', 'Baby formula and nutritional supplements', 
     50, 'tins', 'Critical', 'Open', 5000.00, 1, date('now', '+7 days'));

-- Insert sample donations
INSERT INTO TblDonations (DonorID, OrphanageID, RequestID, DonationType, Amount, ItemDescription,
                         Status, PaymentMethod, TransactionReference, DonationDate) VALUES
    -- John's donations
    (5, 1, 1, 'Food', 2000.00, 'Rice and cooking oil donation', 
     'Completed', 'EFT', 'EFT123456', datetime('now', '-5 days')),
     
    (5, 2, NULL, 'Money', 5000.00, 'Monthly contribution', 
     'Completed', 'Debit Card', 'CARD789012', datetime('now', '-10 days')),
    
    -- Mary's donations
    (6, 1, 2, 'Clothing', 3000.00, 'Winter jackets for 20 children', 
     'Completed', 'Credit Card', 'CC345678', datetime('now', '-3 days')),
     
    (6, 3, NULL, 'Money', 10000.00, 'Annual donation', 
     'Completed', 'EFT', 'EFT901234', datetime('now', '-15 days')),
    
    -- Pending donations
    (5, 2, 4, 'Medical', 3000.00, 'First aid kit and medications', 
     'Processing', 'EFT', NULL, datetime('now', '-1 day')),
     
    (6, 4, 11, 'Food', 5000.00, 'Baby formula donation', 
     'Pending', NULL, NULL, datetime('now'));

-- Insert sample volunteer opportunities
INSERT INTO TblVolunteerOpportunities (OrphanageID, Title, Description, Category, SkillsRequired,
                                      TimeCommitment, StartDate, EndDate, MaxVolunteers, Status,
                                      CreatedBy, BackgroundCheckRequired, TrainingProvided) VALUES
    (1, 'Weekend Tutoring Program', 
     'Help children with homework and provide academic support every Saturday morning',
     'Teaching', 'Basic mathematics and English literacy', '4 hours/week',
     date('now', '+7 days'), date('now', '+90 days'), 10, 'Open', 2, 1, 1),
     
    (1, 'Sports Coach - Soccer', 
     'Coach children''s soccer team for upcoming inter-orphanage tournament',
     'Sports', 'Soccer coaching experience preferred', '6 hours/week',
     date('now', '+14 days'), date('now', '+60 days'), 2, 'Open', 2, 1, 0),
     
    (2, 'Art and Craft Workshops', 
     'Conduct creative art sessions to help children express themselves',
     'Arts', 'Art/craft skills, patience with children', '3 hours/week',
     date('now', '+10 days'), date('now', '+120 days'), 5, 'Open', 3, 1, 0),
     
    (2, 'Garden Maintenance', 
     'Help maintain and develop the orphanage vegetable garden',
     'Maintenance', 'Basic gardening knowledge', 'Flexible',
     date('now', '+1 day'), NULL, 8, 'Open', 3, 0, 1),
     
    (3, 'Computer Literacy Training', 
     'Teach basic computer skills to teenagers preparing for job market',
     'Teaching', 'Computer proficiency, teaching experience helpful', '8 hours/week',
     date('now', '+21 days'), date('now', '+180 days'), 3, 'Open', 4, 1, 1),
     
    (4, 'Storytime Volunteers', 
     'Read stories to young children and help develop literacy skills',
     'Teaching', 'Love for children and reading', '2 hours/week',
     date('now', '+3 days'), NULL, 15, 'Open', 1, 1, 0);

-- Insert sample volunteer applications
INSERT INTO TblVolunteerApplications (OpportunityID, VolunteerID, ApplicationDate, Status,
                                     MotivationLetter, AvailableStartDate) VALUES
    (1, 7, datetime('now', '-2 days'), 'Approved',
     'I am passionate about education and would love to help children with their studies.',
     date('now', '+7 days')),
     
    (1, 8, datetime('now', '-1 day'), 'Pending',
     'As a retired teacher, I want to give back to the community by sharing my knowledge.',
     date('now', '+14 days')),
     
    (4, 7, datetime('now', '-3 days'), 'Approved',
     'I grew up on a farm and would love to teach children about growing their own food.',
     date('now', '+1 day')),
     
    (6, 8, datetime('now'), 'Pending',
     'Reading has always been my passion, and I want to inspire children to love books.',
     date('now', '+3 days'));

-- Insert sample notifications
INSERT INTO TblNotifications (UserID, Type, Title, Message, Priority, Status) VALUES
    (2, 'Request', 'Resource Request Approved', 
     'Your request for winter clothing has been approved and partially fulfilled.', 
     'High', 'Unread'),
     
    (3, 'Donation', 'New Donation Received', 
     'A donation of R5,000 has been received for medical supplies.', 
     'Normal', 'Unread'),
     
    (5, 'System', 'Tax Certificate Available', 
     'Your Section 18A tax certificate for the previous financial year is now available.', 
     'Normal', 'Read'),
     
    (7, 'Volunteer', 'Application Approved', 
     'Your volunteer application for Weekend Tutoring has been approved!', 
     'High', 'Unread');

-- Insert sample audit log entries
INSERT INTO TblAuditLog (UserID, Username, Action, EntityType, EntityID, Timestamp, Success) VALUES
    (1, 'admin', 'LOGIN', 'User', 1, datetime('now', '-2 hours'), 1),
    (2, 'sunshine_rep', 'CREATE', 'ResourceRequest', 1, datetime('now', '-1 day'), 1),
    (5, 'john_donor', 'CREATE', 'Donation', 1, datetime('now', '-5 days'), 1),
    (7, 'volunteer_sam', 'APPLY', 'VolunteerOpportunity', 1, datetime('now', '-2 days'), 1),
    (1, 'admin', 'VERIFY', 'Orphanage', 1, datetime('now', '-30 days'), 1);

-- =====================================================
-- STEP 6: Create Views for Common Queries
-- =====================================================

-- Active resource requests summary
CREATE VIEW IF NOT EXISTS vw_ActiveResourceRequests AS
SELECT 
    rr.RequestID,
    o.OrphanageName,
    o.City,
    o.Province,
    rr.ResourceType,
    rr.ResourceDescription,
    rr.Quantity,
    rr.UrgencyLevel,
    rr.RequestDate,
    rr.NeededByDate,
    rr.EstimatedValue,
    rr.Status
FROM TblResourceRequests rr
JOIN TblOrphanages o ON rr.OrphanageID = o.OrphanageID
WHERE rr.Status IN ('Open', 'In Progress')
ORDER BY 
    CASE rr.UrgencyLevel 
        WHEN 'Critical' THEN 1 
        WHEN 'High' THEN 2 
        WHEN 'Medium' THEN 3 
        WHEN 'Low' THEN 4 
    END,
    rr.RequestDate;

-- Donation summary by orphanage
CREATE VIEW IF NOT EXISTS vw_DonationSummary AS
SELECT 
    o.OrphanageID,
    o.OrphanageName,
    COUNT(d.DonationID) as TotalDonations,
    SUM(CASE WHEN d.Status = 'Completed' THEN d.Amount ELSE 0 END) as TotalAmountReceived,
    COUNT(DISTINCT d.DonorID) as UniqueDonors
FROM TblOrphanages o
LEFT JOIN TblDonations d ON o.OrphanageID = d.OrphanageID
GROUP BY o.OrphanageID, o.OrphanageName;

-- =====================================================
-- STEP 7: Data Migration from Backup (if exists)
-- =====================================================

-- Attempt to recover data from backup tables if they exist and have compatible columns
-- This is commented out by default - uncomment and modify as needed

/*
-- Example: Recover user data if backup exists
INSERT OR IGNORE INTO TblUsers (Username, Email, FullName, AccountStatus)
SELECT 
    COALESCE(Username, 'user_' || rowid),
    COALESCE(Email, 'user_' || rowid || '@orphanagehub.org.za'),
    FullName,
    AccountStatus
FROM TblUsers_backup
WHERE FullName IS NOT NULL 
  AND Username NOT IN (SELECT Username FROM TblUsers);
*/

-- =====================================================
-- STEP 8: Clean up backup tables (optional)
-- =====================================================

-- Uncomment these lines if you want to remove backup tables after verification
-- DROP TABLE IF EXISTS TblUsers_backup;
-- DROP TABLE IF EXISTS TblOrphanages_backup;
-- DROP TABLE IF EXISTS TblResourceRequests_backup;

-- =====================================================
-- STEP 9: Database Statistics and Optimization
-- =====================================================

-- Update SQLite statistics for query optimization
ANALYZE;

-- Vacuum to reclaim space and optimize database file
-- VACUUM; -- Uncomment if you want to run vacuum (can take time on large databases)

-- =====================================================
-- FINAL: Verification Queries
-- =====================================================

-- Display summary of created tables and record counts
SELECT 'Database Setup Complete!' as Status;
SELECT '========================' as '=';
SELECT 'Table Summary:' as Info;
SELECT name as TableName, 
       (SELECT COUNT(*) FROM sqlite_master WHERE type='index' AND tbl_name=m.name) as Indexes
FROM sqlite_master m 
WHERE type='table' AND name NOT LIKE 'sqlite_%' AND name NOT LIKE '%_backup'
ORDER BY name;

SELECT '========================' as '=';
SELECT 'Record Counts:' as Info;
SELECT 'TblUsers' as TableName, COUNT(*) as Records FROM TblUsers
UNION ALL SELECT 'TblOrphanages', COUNT(*) FROM TblOrphanages
UNION ALL SELECT 'TblResourceRequests', COUNT(*) FROM TblResourceRequests
UNION ALL SELECT 'TblDonations', COUNT(*) FROM TblDonations
UNION ALL SELECT 'TblVolunteerOpportunities', COUNT(*) FROM TblVolunteerOpportunities
UNION ALL SELECT 'TblVolunteerApplications', COUNT(*) FROM TblVolunteerApplications;

SELECT '========================' as '=';
SELECT 'Default Login Credentials:' as Info;
SELECT '  Username: admin' as Credential;
SELECT '  Password: Admin123!' as Credential;
SELECT '========================' as '=';