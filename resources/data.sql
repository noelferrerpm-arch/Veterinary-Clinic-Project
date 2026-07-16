SELECT *
FROM Administrator;

INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('Alice', 'Smith', '123-456-7890', 'alice.smith"@gmail.com', '123 Maple St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');
--
INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('Bob', 'Johnson', '234-567-8901', 'bob.john@gmail.com', '456 Oak St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');

INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('Eve', 'Davis', '345-678-9012', 'eve@gmail.com', '789 Pine St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');

INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('Charlie', 'Brown', '456-789-0123', 'charlie.b@gmail.com' , '101 Cedar St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');

INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('David', 'Wilson', '567-890-1234', 'david.w@gmail.com', '202 Birch St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');

INSERT INTO Person (first_name, last_name, phone_number, email, address, password)
VALUES ('Fiona', 'Garcia', '678-901-2345', 'fionag@gmail.com', '303 Walnut St, Springfield, IL', '$2a$12$p/FXJi9lu2UDncqizrGvNekDXKeo5/xR4A315amMv1I/E3ebEPPY2');

INSERT INTO Veterinarian (person_id, license_number, years_of_experience)
VALUES (1,  44, 4);
-- INSERT INTO Veterinarian (person_id, license_number, years_of_experience)
-- VALUES (4,55, 5);


INSERT INTO availability (
    day_of_week,
    start_time,
    end_time,
    period_start,
    period_end,
    veterinarian_person_id
) VALUES
      (1, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (2, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (3, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (4, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (5, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (6, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1),
      (7, '00:00:00', '23:59:59', '2024-01-01', '2027-03-01', 1);


INSERT INTO agenda (
    veterinarian_person_id,
    agenda_year
) VALUES (
             1,
             2023
         ),
         (
             1,
             2024
         ),
         (
             1,
             2025
         );

INSERT INTO Loyalty_Tier (tier_name, required_points, discount_percentage, benefits_description)
VALUES ('GOLD', 3, 0.15, '15% discount on all services, priority booking');

INSERT INTO Pet_Owner (person_id, loyalty_points, loyalty_tier_id)
VALUES (2, 50, 1);

INSERT INTO Pet_Type (type_id, name, description)
VALUES (1, 'Dog', 'Domestic canine');

INSERT INTO Pet (name, date_of_birth, gender, breed, color, weight, microchip_id, pet_owner_id, pet_type_type_id)
VALUES ('Buddy', '2020-05-15', 'Male', 'Golden Retriever', 'Golden', 30.0, 'MC123456', 2, 1);

INSERT INTO Visit (start_date, duration, reason, price, pet_id, owner_id, veterinarian_id, agenda_id, status)
VALUES ('2024-07-01', 15, 'pata', 30.50, 1, 2, 1, 2, 'Scheduled'),
       ('2024-07-01', 15, 'algo', 30.50, 1, 2, 1, 2, 'Scheduled'),
       ('2025-07-01', 15, 'pata', 30.50, 1, 2, 1, 3, 'Scheduled'),
        ('2025-07-02', 15, 'pata', 30.50, 1, 2, 1, 3, 'Completed');

INSERT INTO Visit (start_date, duration, reason, price, pet_id, owner_id, veterinarian_id, agenda_id, status)
VALUES ('2024-07-02', 2, 'cola', 30.50, 1, 2, 1, 2, 'Scheduled'),
       ('2024-07-02', 2, 'cola', 30.50, 1, 2, 1, 2, 'In_Progress');



INSERT INTO role (name) VALUES ('ADMIN');
INSERT INTO role (name) VALUES ('RECEPTIONIST');
INSERT INTO role (name) VALUES ('VETERINARIAN');
INSERT INTO role (name) VALUES ('VET_ASSISTANT');
INSERT INTO role (name) VALUES ('PET_OWNER');
INSERT INTO role (name) VALUES ('INVENTORY_MANAGER');

INSERT INTO person_roles(person_id, role_id) VALUES(1,3);

INSERT INTO person_roles(person_id, role_id) VALUES(2,5);

INSERT INTO receptionist (person_id, receptionist)
VALUES (3, 'Recepcionista1');

INSERT INTO person_roles(person_id, role_id) VALUES(3,2);

INSERT INTO person_roles(person_id, role_id) VALUES(4,4);

INSERT INTO person_roles(person_id, role_id) VALUES(5,1);

INSERT INTO person_roles(person_id, role_id) VALUES(6,6);

INSERT INTO MEDICATION (name, dosage_unit, unit_price,active_ingredient ) VALUES( 'Ibuprofeno', 1, 10,'Cosas');

INSERT INTO MEDICATION (name, dosage_unit, unit_price,active_ingredient ) VALUES( 'Paracetamol', 2, 20,'Cosas');

INSERT INTO MEDICATION (name, dosage_unit, unit_price,active_ingredient ) VALUES( 'Diboxufilin', 3, 30,'Cosas');

INSERT INTO MEDICATION (name, dosage_unit, unit_price,active_ingredient ) VALUES( 'Ribonumol', 4, 40,'Cosas');
INSERT INTO medication_batch (
    medication_id,
    lot_number,
    received_date,
    expiry_date,
    initial_quantity,
    current_quantity,
    purchage_price_per_unit,
    storage_location,
    reorder_threshold
)
VALUES (
           1,  -- medication_id (Paracetamol)
           2,  -- lot_number
           '2024-02-01',  -- received_date
           '2029-12-31',  -- expiry_date
           150,  -- initial_quantity
           150,  -- current_quantity
           2.00,  -- purchage_price_per_unit
           'Shelf B2',  -- storage_location
           25   -- reorder_threshold
       );
