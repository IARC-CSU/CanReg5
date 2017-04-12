## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
Args <- commandArgs(TRUE)


## To get the R folder of the actual script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", 
                   initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)
source(paste(sep="/", script.basename, "Rcan_source.r"))
################


cat(paste("-outFile",filename,sep=":"))


