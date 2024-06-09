create table if not exists loans (
  id int not null auto_increment primary key,
  mobile_number varchar(20) not null,
  loan_number varchar(100) not null,
  loan_type varchar(100) not null,
  total_loan int not null,
  amount_paid int not null,
  outstanding_amount int not null,
  created_at date not null,
  created_by varchar(30) not null,
  updated_at date default null,
  updated_by varchar(30) default null
);
