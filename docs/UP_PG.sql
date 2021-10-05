create extension pg_stat_statements;
select * from pg_available_extensions where name = 'pg_stat_statements' and installed_version is not null;
select substring(query, 1, 500) as query_truncated, round(total_exec_time::numeric, 2) as total_time, calls, round(mean_exec_time::numeric, 2) as mean, round( (100* total_exec_time / sum(total_exec_time::numeric) OVER ())::numeric, 2) as overall_percentage from public.pg_stat_statements order by total_time desc limit 20;
SELECT pg_stat_statements_reset();

set schema 'underpayments';

SELECT count(*) FROM account;
SELECT count(*) FROM relationship;
SELECT count(*) FROM sp_award;
SELECT * FROM circumstance;

-- DROP SCHEMA underpayments CASCADE; CREATE SCHEMA underpayments;
CREATE UNIQUE INDEX citizen_key_idx ON underpayments.account (citizen_key);
CREATE INDEX ac_pk_prsn_idx ON underpayments.account (pk_prsn);
CREATE INDEX reln_pk_prsn_idx ON underpayments.relationship (pk_prsn);
CREATE INDEX reln_pk_prsn_b_idx ON underpayments.relationship (pk_prsnb);
CREATE INDEX aw_pk_prsn_idx ON underpayments.sp_award (pk_prsn);
CREATE INDEX step_completed_idx ON underpayments.account (step_completed);
CREATE INDEX calc_result_code_idx ON underpayments.account (calc_result_code);


update account
set calc_result_code = 1
where calc_result_code = 2;

select count(*) 
from award;


update account
set step_completed = 4
where step_completed = 5;

-- **

select * from account 
limit 10;


select * from account limit 100;
select count(*) from sp_award limit 100;
delete from sp_award;


update relationship set fk_account = 903 where pk_prsn='AJ528881';

select count(*) from relationship where fk_account is not null;
select count(*) from sp_award where fk_account is not null;

select count(*) from account where has_errors = false;

select count(*) from account where calc_result_code = 2;


select * from account limit 100;
select * from relationship limit 100;
select * from sp_award limit 100;

select * from relationship where fk_account = 2810;

update account
set calc_result_code = 0
where calc_result_code = 1;

select distinct xi_area_id from account;

