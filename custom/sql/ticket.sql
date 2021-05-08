-----------------------------------
SELECT * FROM test.ticket;
-----------------------------------
delete from test.ticket;
-----------------------------------
delete from test.ticket where out_time is null;
-----------------------------------
INSERT INTO test.ticket
            (id,
             parking_number,
             vehicle_reg_number,
             price,
             in_time,
             out_time)
SELECT Ifnull(Max(id), 0) + 1,
       (SELECT Min(parking_number)
        FROM   parking
        WHERE  available = true),
       "ABCDEF",
       0,
       -- NOW()- interval 1 HOUR
       Date_sub(Now(), INTERVAL 15 minute),
       NULL
FROM   test.ticket ticket; 
-----------------------------------
