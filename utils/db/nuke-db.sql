drop database if exists medianoche;
revoke all on schema public from medianoche;
drop role if exists medianoche;
create database medianoche;
create user medianoche with password 'medianoche';
grant all on database medianoche to medianoche;
grant all on schema public to medianoche;
revoke all on database postgres from medianoche;
