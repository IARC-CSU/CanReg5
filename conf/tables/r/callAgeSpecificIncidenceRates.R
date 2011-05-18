# 1st argument is either pdf, png, ps or svg
# 2nd argument is report file name - or output file name if you want
# 3rd argument is population file name
# 4th argument is incidence file name

Args <- commandArgs(TRUE)
# garbage <- dev.off()

if (Args[1] == "png") {
    filename <- paste( Args[2], ".png", sep = "" )
    png(file=filename, bg="transparent")
} else if (Args[1] == "pdf") { 
    filename <- paste( Args[2], ".pdf" , sep = "")
    pdf(file=filename) 
} else { 
    filename <- paste( Args[2], ".png" , sep = "")
    png(file=filename, bg="transparent") 
}

# Find the directory of the script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))

fileInc <- Args[4]
dataInc <- read.table(fileInc, header=TRUE)
colnames(dataInc)

filePop <- Args[3]
dataPop <- read.table(filePop, header=TRUE)

figure_AgeSpecificIncidenceRates("fileType", dataInc, dataPop)
	
dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])