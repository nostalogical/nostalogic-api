-- Wipe all data from all tables and then drop the test schema
DROP TABLE IF EXISTS navigation_link_mask;
DROP TABLE IF EXISTS navigation_link;

DROP TABLE IF EXISTS container;
DROP TABLE IF EXISTS navigation;
DROP TABLE IF EXISTS article_revision;
DROP TABLE IF EXISTS article;

DROP SCHEMA IF EXISTS test_nostalogic_content CASCADE ;
