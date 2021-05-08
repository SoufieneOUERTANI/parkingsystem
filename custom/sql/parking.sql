-----------------------------------
SELECT * FROM test.parking;
-----------------------------------
update test.parking set available = true;
-----------------------------------
update 
test.parking A
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
		test.parking C
		where 
		C.AVAILABLE = true
	) B
)


