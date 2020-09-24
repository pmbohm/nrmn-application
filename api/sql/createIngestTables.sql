     alter table if exists nrmn.error_check
        drop constraint if exists FK_ERROR_STAGED_SURVEY;

    alter table if exists nrmn.staged_survey
        drop constraint if exists PK_STAGED_SURVEY_STAGED_JOB;

    drop table if exists nrmn.error_check cascade;

    drop table if exists nrmn.staged_job cascade;

    drop table if exists nrmn.staged_survey cascade;

     create table nrmn.error_check (
                                       job_id varchar(255) not null,
                                       message varchar(255) not null,
                                       column_target varchar(255),
                                       error_level varchar(255),
                                       row_id int8 not null,
                                       primary key (job_id, message, row_id)
     );


     create table nrmn.staged_job (
                                      file_id varchar(255) not null,
                                      job_attributes uuid,
                                      source varchar(255),
                                      status varchar(255),
                                      primary key (file_id)
     );

     create table nrmn.staged_survey (
                                         id  bigserial not null,
                                         common_name varchar(255),
                                         L5 int4,
                                         L95 int4,
                                         PQs int4,
                                         block int4,
                                         buddy varchar(255),
                                         code varchar(255),
                                         date date,
                                         depth float8,
                                         direction varchar(255),
                                         diver varchar(255),
                                         inverts int4,
                                         is_invert_Sizing boolean,
                                         latitude float8,
                                         longitude float8,
                                         m2_invert_sizing_species boolean,
                                         measure_value json,
                                         method int4,
                                         site_name varchar(255),
                                         site_no varchar(255),
                                         species varchar(255),
                                         time float8,
                                         total int4,
                                         vis int4,
                                         stagedJob_file_id varchar(255),
                                         primary key (id)
     );

     alter table if exists nrmn.error_check
        add constraint FK_ERROR_STAGED_SURVEY
            foreign key (row_id)
                references nrmn.staged_survey;

    alter table if exists nrmn.staged_survey
        add constraint PK_STAGED_SURVEY_STAGED_JOB
            foreign key (stagedJob_file_id)
                references nrmn.staged_job;