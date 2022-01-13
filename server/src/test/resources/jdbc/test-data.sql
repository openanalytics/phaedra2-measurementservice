TRUNCATE measservice.measurement RESTART IDENTITY CASCADE;

insert into measservice.measurement
values (1, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null);

insert into measservice.measurement
values (2, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null);

insert into measservice.measurement
values (3, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null);
