-- Rollback any broken transaction if needed
ROLLBACK;

-- Truncate all tables and cascade to dependent foreign keys
TRUNCATE TABLE
    SERVICECOST,
    TRAVEL,
    TOPUP,
    REPLACEMENT,
    REPLACEMENTORDER,
    DOCK,
    SCOOTER,
    SCOOTERMODEL,
    STATION,
    CARD,
    CLIENT,
    EMPLOYEE,
    PERSON,
    TYPEOFCARD
RESTART IDENTITY CASCADE;
