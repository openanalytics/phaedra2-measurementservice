TRUNCATE measservice.measurement RESTART IDENTITY CASCADE;

insert into measservice.measurement
values (1000, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null,1);

insert into measservice.measurement
values (2000, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null,1);

insert into measservice.measurement
values (3000, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null,1);

insert into measservice.measurement
values (4000, 'TestBySaša', 'SBETST0001', 'Test by Saša', 16, 24, now(), 'Anonymous', '{wellCol1,wellCol2,wellCol3,wellCol4}', '{subWellCol1,subWellCol2,subWellCol3,subWellCol4}', null,null);
