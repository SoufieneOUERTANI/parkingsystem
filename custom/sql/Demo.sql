select * from prod.parking;
select * from prod.ticket;
-----------------------------------
SET SQL_SAFE_UPDATES = 0;
-----------------------------------
delete from prod.ticket;
update prod.parking set available = true;
select * from prod.ticket;
select * from prod.parking;

-----------------------------------
--- Cas d un BIKE entre il y a plus de 30 min
-----------------------------------

INSERT INTO prod.ticket
            (id,
             parking_number,
             vehicle_reg_number,
             price,
             in_time,
             out_time)
SELECT Ifnull(Max(id), 0) + 1,
       (SELECT Min(parking_number)
        FROM   prod.parking
        WHERE  available = true and type = "BIKE"),
       "YUIOP",
       0,
       -- NOW()- interval 1 HOUR
       Date_sub(Now(), INTERVAL 120+30 minute),
       NULL
FROM   prod.ticket; 
-----------------------------------
update 
prod.parking A
set 
A.available = false
where 
A.PARKING_NUMBER =
(
	select 
	min(B.PARKING_NUMBER)
	from 
    (
		select 
		C.PARKING_NUMBER 
		from 
		prod.parking C
		where 
		C.AVAILABLE = true and TYPE = "BIKE"
	) B
);

-----------------------------------
--- Cas d un CAR recurent qui est rentre et sorti hier 
-----------------------------------

INSERT INTO prod.ticket
            (id,
             parking_number,
             vehicle_reg_number,
             price,
             in_time,
             out_time)
SELECT Ifnull(Max(id), 0) + 1,
       (SELECT Min(parking_number)
        FROM   prod.parking
        WHERE  available = true  and type = "CAR"),
       "QSDFG",
       0,
       -- NOW()- interval 1 HOUR
       Date_sub(Now(), INTERVAL (24*(60))+120+1+60 minute),
       Date_sub(Now(), INTERVAL (24*(60))+120+1 minute)
FROM   prod.ticket; 
-----------------------------------
update 
prod.parking A
set 
A.available = false
where 
A.PARKING_NUMBER =
(
	select 
	min(B.PARKING_NUMBER)
	from 
    (
		select 
		C.PARKING_NUMBER 
		from 
		prod.parking C
		where 
		C.AVAILABLE = true and TYPE = "CAR"
	) B
);
-----------------------------------
--- Cas d un CAR recurent qui est déjà réentré aujourdhui il y a plus de 30 min
-----------------------------------

INSERT INTO prod.ticket
            (id,
             parking_number,
             vehicle_reg_number,
             price,
             in_time,
             out_time)
SELECT Ifnull(Max(id), 0) + 1,
       (SELECT Min(parking_number)
        FROM   prod.parking
        WHERE  available = true  and type = "BIKE"),
       "ABCDE",
       0,
       -- NOW()- interval 1 HOUR
       Date_sub(Now(), INTERVAL (24*(60))+120+1+60 minute),
       Date_sub(Now(), INTERVAL (24*(60))+120+1 minute)
FROM   prod.ticket; 
-----------------------------------
update 
prod.parking A
set 
A.available = false
where 
A.PARKING_NUMBER =
(
	select 
	min(B.PARKING_NUMBER)
	from 
    (
		select 
		C.PARKING_NUMBER 
		from 
		prod.parking C
		where 
		C.AVAILABLE = true and TYPE = "BIKE"
	) B
);

INSERT INTO prod.ticket
            (id,
             parking_number,
             vehicle_reg_number,
             price,
             in_time,
             out_time)
SELECT Ifnull(Max(id), 0) + 1,
       (SELECT Min(parking_number)
        FROM   prod.parking
        WHERE  available = true  and type = "BIKE"),
       "ABCDE",
       0,
       -- NOW()- interval 1 HOUR
       Date_sub(Now(), INTERVAL 120+120+1 minute),
       null
FROM   prod.ticket; 
-----------------------------------
update 
prod.parking A
set 
A.available = false
where 
A.PARKING_NUMBER =
(
	select 
	min(B.PARKING_NUMBER)
	from 
    (
		select 
		C.PARKING_NUMBER 
		from 
		prod.parking C
		where 
		C.AVAILABLE = true and TYPE = "BIKE"
	) B
);
-----------------------------------
select * from prod.parking;
select * from prod.ticket;
