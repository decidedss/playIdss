# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table gis.public._actions (
  id                        integer not null,
  phenomenon                varchar(255),
  category                  varchar(255),
  description               varchar(255),
  phase                     varchar(255),
  body                      varchar(255),
  implementing_body         varchar(255),
  participating_body        varchar(255),
  agency                    varchar(255),
  constraint pk__actions primary key (id))
;

create table gis.public._actions_implementing_body (
  id                        integer not null,
  title                     varchar(255),
  agency                    varchar(255),
  constraint pk__actions_implementing_body primary key (id))
;

create table gis.public._actions_phase (
  id                        integer not null,
  title                     varchar(255),
  title_en                  varchar(255),
  agency                    varchar(255),
  constraint pk__actions_phase primary key (id))
;

create table gis.public._actions_phenomenon (
  id                        integer not null,
  title                     varchar(255),
  agency                    varchar(255),
  constraint pk__actions_phenomenon primary key (id))
;

create table gis.public._meteo_currentmonth (
  id                        integer not null,
  "day"                     integer,
  mean_temp                 float,
  high                      float,
  low                       float,
  rain                      float,
  avg_wind_speed            float,
  hight_wind                float,
  time_wind                 timestamp,
  dom_dir                   varchar(255),
  month                     integer,
  station                   varchar(255),
  constraint pk__meteo_currentmonth primary key (id))
;

create table gis.public._contacts (
  id                        integer not null,
  lastname                  varchar(255),
  firstname                 varchar(255),
  email                     varchar(255),
  profession                varchar(255),
  position                  varchar(255),
  mobile                    varchar(255),
  username                  varchar(255),
  agency                    varchar(255),
  group_id                  integer,
  constraint uq__contacts_mobile unique (mobile),
  constraint pk__contacts primary key (id))
;

create table gis.public._contacts_alfresco (
  contact_id                integer not null,
  username                  varchar(255),
  group_id                  integer,
  agency                    varchar(255),
  constraint pk__contacts_alfresco primary key (contact_id))
;

create table gis.public._contacts_groups (
  id                        integer not null,
  groupname                 varchar(255),
  agency                    varchar(255),
  constraint pk__contacts_groups primary key (id))
;

create table gis.public._disasters_attribute_mapping (
  disaster_type             varchar(255) not null,
  disaster_area             varchar(255),
  disaster_cause            varchar(255),
  disaster_characteristics  varchar(255),
  disaster_impacts_infrastructure varchar(255),
  disaster_impacts_other    varchar(255),
  disaster_duration         varchar(255),
  constraint pk__disasters_attribute_mapping primary key (disaster_type))
;

create table gis.public._event_types (
  id                        integer not null,
  code                      varchar(255),
  description               varchar(255),
  description_en            varchar(255),
  username                  varchar(255),
  icon                      varchar(255),
  icon_thumbnail            varchar(255),
  disaster_description      varchar(255),
  disaster_description_en   varchar(255),
  disaster_icon             varchar(255),
  disaster_icon_thumbnail   varchar(255),
  constraint pk__event_types primary key (id))
;

create table gis.public._forecast_capacitation (
  id                        integer not null,
  station                   varchar(255),
  value                     float,
  "date"                    varchar(255),
  hours                     integer,
  hours_mapping             varchar(255),
  constraint pk__forecast_capacitation primary key (id))
;

create table gis.public._forecast_precipitation (
  id                        integer not null,
  station                   varchar(255),
  value                     float,
  "date"                    varchar(255),
  hours                     integer,
  hours_mapping             varchar(255),
  constraint pk__forecast_precipitation primary key (id))
;

create table gis.public._forecast_stations (
  id                        integer not null,
  place                     varchar(255),
  title                     varchar(255),
  lat                       float,
  lon                       float,
  constraint pk__forecast_stations primary key (id))
;

create table gis.public._forecast_temperature (
  id                        integer not null,
  station                   varchar(255),
  value                     float,
  "date"                    varchar(255),
  hours                     integer,
  hours_mapping             varchar(255),
  constraint pk__forecast_temperature primary key (id))
;

create table gis.public._forecast_wind (
  id                        integer not null,
  station                   varchar(255),
  value_degrees             float,
  value_speed               float,
  "date"                    varchar(255),
  hours                     integer,
  hours_mapping             varchar(255),
  constraint pk__forecast_wind primary key (id))
;

create table gis.public._geodata_layers (
  id                        integer not null,
  groupname                 varchar(255),
  groupname_en              varchar(255),
  tablename                 varchar(255),
  layertitle                varchar(255),
  layerid                   varchar(255),
  visibleid                 varchar(255),
  constraint pk__geodata_layers primary key (id))
;

create table gis.public._meteo_current_year_precipitation (
  id                        integer not null,
  year                      varchar(255),
  month                     varchar(255),
  rainfall                  float,
  maxobsday                 float,
  day_rainfall              varchar(255),
  days_rainover_01          varchar(255),
  days_rainover_02          varchar(255),
  days_rainover_03          varchar(255),
  station                   varchar(255),
  constraint pk__meteo_current_year_precipita primary key (id))
;

create table gis.public._meteo_current_year_temperature (
  id                        integer not null,
  year                      varchar(255),
  month                     varchar(255),
  mean_max                  float,
  mean_min                  float,
  mean                      float,
  station                   varchar(255),
  constraint pk__meteo_current_year_temperatu primary key (id))
;

create table gis.public._meteo_current_year_wind (
  id                        integer not null,
  year                      varchar(255),
  month                     varchar(255),
  avg_speed                 float,
  dom_dir                   varchar(255),
  station                   varchar(255),
  constraint pk__meteo_current_year_wind primary key (id))
;

create table gis.public._infrastructure_attributes_mapping (
  id                        integer not null,
  layer_id                  varchar(255),
  attributes                varchar(255),
  attributes_en             varchar(255),
  constraint pk__infrastructure_attributes_ma primary key (id))
;

create table gis.public._infrastructure_mapping (
  id                        integer not null,
  layer_id                  varchar(255),
  layer_title               varchar(255),
  layer_title_en            varchar(255),
  geometry_type             varchar(255),
  constraint pk__infrastructure_mapping primary key (id))
;

create table gis.public._meteo_last2days (
  id                        integer not null,
  day                       timestamp,
  time                      timestamp,
  temp_out                  float,
  wind_speed                float,
  wind_dir                  varchar(255),
  rain                      float,
  station                   varchar(255),
  constraint pk__meteo_last2days primary key (id))
;

create table gis.public._machinery_layer (
  id                        integer not null,
  machinery_id              integer,
  brand                     varchar(255),
  licence_plate             varchar(255),
  bhp                       varchar(255),
  seats                     varchar(255),
  equipment                 varchar(255),
  cargo_type                varchar(255),
  driver                    varchar(255),
  disaster_type             varchar(255),
  machinery_status          varchar(255),
  tires_status              varchar(255),
  capacity_m3               varchar(255),
  notes                     varchar(255),
  username                  varchar(255),
  agency                    varchar(255),
  availability              varchar(255),
  constraint pk__machinery_layer primary key (id))
;

create table gis.public._machinery_type (
  id                        integer not null,
  vehicle_type              varchar(255),
  username                  varchar(255),
  agency                    varchar(255),
  icon                      varchar(255),
  thumbnail                 varchar(255),
  constraint pk__machinery_type primary key (id))
;

create table gis.public._measures (
  id                        integer not null,
  name                      varchar(255),
  category                  varchar(255),
  location                  varchar(255),
  description               varchar(255),
  budget                    varchar(255),
  riskaddressing            varchar(255),
  startdate                 timestamp,
  enddate                   timestamp,
  agency                    varchar(255),
  constraint pk__measures primary key (id))
;

create table gis.public._measures_categories (
  id                        integer not null,
  title_en                  varchar(255),
  title_el                  varchar(255),
  constraint pk__measures_categories primary key (id))
;

create table gis.public._meteo_stations (
  id                        integer not null,
  place                     varchar(255),
  title                     varchar(255),
  lat                       float,
  lon                       float,
  constraint pk__meteo_stations primary key (id))
;

create table gis.public._notifications (
  id                        integer not null,
  descr                     varchar(255),
  type                      varchar(255),
  username                  varchar(255),
  agency                    varchar(255),
  is_disaster               boolean,
  lat                       float,
  lon                       float,
  image                     varchar(255),
  insert_date               timestamp,
  disaster_date             varchar(255),
  disaster_duration         varchar(255),
  disaster_area             varchar(255),
  disaster_cause            varchar(255),
  disaster_characteristics  varchar(255),
  disaster_injuries         varchar(255),
  disaster_deaths           varchar(255),
  disaster_impacts_infrastructure varchar(255),
  disaster_impacts_other    varchar(255),
  disaster_means_forces     varchar(255),
  disaster_action_list      varchar(255),
  disaster_remarks          varchar(255),
  disaster_suggestions      varchar(255),
  constraint pk__notifications primary key (id))
;

create table gis.public._agency_sharing (
  id                        integer not null,
  agency                    varchar(255),
  agency_displayname        varchar(255),
  share                     boolean,
  insert_date               timestamp,
  constraint pk__agency_sharing primary key (id))
;

create table gis.public._sms (
  id                        integer not null,
  message                   varchar(255),
  username                  varchar(255),
  insert_date               timestamp,
  constraint pk__sms primary key (id))
;

create table gis.public._traffic_way (
  id                        integer not null,
  link_id                   varchar(255),
  node                      varchar(255),
  lat                       float,
  lon                       float,
  constraint pk__traffic_way primary key (id))
;

create sequence _actions_id_seq;

create sequence _actions_implementing_body_seq;

create sequence _actions_phase_seq;

create sequence _actions_phenomenon_seq;

create sequence _meteo_currentmonth_id_seq;

create sequence _contacts_id_seq;

create sequence _contacts_alfresco_id_seq;

create sequence _contacts_groups_id_seq;

create sequence _disasters_attributes_mapping_id_seq;

create sequence _event_types_id_seq;

create sequence _forecast_capacitation_id_seq;

create sequence _forecast_precipitation_id_seq;

create sequence _forecast_stations_id_seq;

create sequence _forecast_temperature_id_seq;

create sequence _forecast_wind_id_seq;

create sequence _geodata_layers_id_seq;

create sequence _meteo_current_year_precipitation_id_seq;

create sequence _meteo_current_year_temperature_id_seq;

create sequence _meteo_current_year_wind_id_seq;

create sequence _infrastructure_attributes_mapping_id_seq;

create sequence _infrastructure_mapping_id_seq;

create sequence _meteo_last2days_id_seq;

create sequence _machinery_layer_id_seq;

create sequence _machinery_id_seq;

create sequence _measures_id_seq;

create sequence _measures_categories_id_seq;

create sequence _meteo_stations_id_seq;

create sequence _notifications_id_seq;

create sequence _agency_sharing_id_seq;

create sequence _sms_id_seq;

create sequence _traffic_way_id_seq;




# --- !Downs

drop table if exists gis.public._actions cascade;

drop table if exists gis.public._actions_implementing_body cascade;

drop table if exists gis.public._actions_phase cascade;

drop table if exists gis.public._actions_phenomenon cascade;

drop table if exists gis.public._meteo_currentmonth cascade;

drop table if exists gis.public._contacts cascade;

drop table if exists gis.public._contacts_alfresco cascade;

drop table if exists gis.public._contacts_groups cascade;

drop table if exists gis.public._disasters_attribute_mapping cascade;

drop table if exists gis.public._event_types cascade;

drop table if exists gis.public._forecast_capacitation cascade;

drop table if exists gis.public._forecast_precipitation cascade;

drop table if exists gis.public._forecast_stations cascade;

drop table if exists gis.public._forecast_temperature cascade;

drop table if exists gis.public._forecast_wind cascade;

drop table if exists gis.public._geodata_layers cascade;

drop table if exists gis.public._meteo_current_year_precipitation cascade;

drop table if exists gis.public._meteo_current_year_temperature cascade;

drop table if exists gis.public._meteo_current_year_wind cascade;

drop table if exists gis.public._infrastructure_attributes_mapping cascade;

drop table if exists gis.public._infrastructure_mapping cascade;

drop table if exists gis.public._meteo_last2days cascade;

drop table if exists gis.public._machinery_layer cascade;

drop table if exists gis.public._machinery_type cascade;

drop table if exists gis.public._measures cascade;

drop table if exists gis.public._measures_categories cascade;

drop table if exists gis.public._meteo_stations cascade;

drop table if exists gis.public._notifications cascade;

drop table if exists gis.public._agency_sharing cascade;

drop table if exists gis.public._sms cascade;

drop table if exists gis.public._traffic_way cascade;

drop sequence if exists _actions_id_seq;

drop sequence if exists _actions_implementing_body_seq;

drop sequence if exists _actions_phase_seq;

drop sequence if exists _actions_phenomenon_seq;

drop sequence if exists _meteo_currentmonth_id_seq;

drop sequence if exists _contacts_id_seq;

drop sequence if exists _contacts_alfresco_id_seq;

drop sequence if exists _contacts_groups_id_seq;

drop sequence if exists _disasters_attributes_mapping_id_seq;

drop sequence if exists _event_types_id_seq;

drop sequence if exists _forecast_capacitation_id_seq;

drop sequence if exists _forecast_precipitation_id_seq;

drop sequence if exists _forecast_stations_id_seq;

drop sequence if exists _forecast_temperature_id_seq;

drop sequence if exists _forecast_wind_id_seq;

drop sequence if exists _geodata_layers_id_seq;

drop sequence if exists _meteo_current_year_precipitation_id_seq;

drop sequence if exists _meteo_current_year_temperature_id_seq;

drop sequence if exists _meteo_current_year_wind_id_seq;

drop sequence if exists _infrastructure_attributes_mapping_id_seq;

drop sequence if exists _infrastructure_mapping_id_seq;

drop sequence if exists _meteo_last2days_id_seq;

drop sequence if exists _machinery_layer_id_seq;

drop sequence if exists _machinery_id_seq;

drop sequence if exists _measures_id_seq;

drop sequence if exists _measures_categories_id_seq;

drop sequence if exists _meteo_stations_id_seq;

drop sequence if exists _notifications_id_seq;

drop sequence if exists _agency_sharing_id_seq;

drop sequence if exists _sms_id_seq;

drop sequence if exists _traffic_way_id_seq;

