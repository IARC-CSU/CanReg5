# This is just a script to test if R is properly installed.
Args <- commandArgs(TRUE)

library("RJSONIO")
library("jsonlite")
# Find the directory of the script
#############################################################
initial.options <- commandArgs(trailingOnly = FALSE)
JSON.path <- "-paramsFile="
paramsJSON <- fromJSON(sub(JSON.path, "", initial.options[grep(JSON.path, initial.options)]))
#############################################################
cat(paste("-outFile",paramsJSON$patientFilePath,sep=":"))
cat("\n")
cat(paste("-outFile",paramsJSON$tumourFilePath,sep=":"))
cat("\n")
cat(paste("-outFile",paramsJSON$sourceFilePath,sep=":"))