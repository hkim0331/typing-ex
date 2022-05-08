#!/bin/sh
export TAOENSSO_TIMBRE_NS_PATTERN_EDN='{:deny #{"duct.database.sql.hikaricp" "duct.middleware.web"}}'
yarn release
lein uberjar
