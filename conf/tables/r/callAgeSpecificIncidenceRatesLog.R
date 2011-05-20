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
} else if (Args[1] == "svg") { 
	# svg needs the RSvgDevice library installed
    filename <- paste( Args[2], ".svg" , sep = "")
	require(RSvgDevice)
    devSVG(file=filename) 
} else if (Args[1] == "ps") { 
    filename <- paste( Args[2], ".ps" , sep = "")
    postscript(file=filename) 
} else if (Args[1] == "wmf") { 
	# This only works on windows
    filename <- paste( Args[2], ".wmf" , sep = "")
    win.metafile(file=filename) 
} else { 
	# defaults to pdf
    filename <- paste( Args[2], ".pdf" , sep = "")
    pdf(file=filename) 
}

# Find the directory of the script
initial.options <- commandArgs(trailingOnly = FALSE)
file.arg.name <- "--file="
script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
script.basename <- dirname(script.name)

# Load dependencies
source(paste(sep="/", script.basename, "figure_AgeSpecificIncidenceRates.R"))
source(paste(sep="/", script.basename, "subsetSex.R"))	
source(paste(sep="/", script.basename, "mergePeriods.R"))
source(paste(sep="/", script.basename, "makeageSpecIncRates.R"))
source(paste(sep="/", script.basename, "subsetSite.R"))
source(paste(sep="/", script.basename, "plotAgeSpecIncRates.R"))
source(paste(sep="/", script.basename, "plotLogAgeSpecIncRates.r"))

# The incidence file is the 4th argument
fileInc <- Args[4]
dataInc <- read.table(fileInc, header=TRUE)
# colnames(dataInc)

# The population file is the 3rd argument
filePop <- Args[3]
dataPop <- read.table(filePop, header=TRUE)

#The variable log has to be declared as TRUE or False wether or not the users wants figures with log rates
#Default is FALSE
figure_AgeSpecificIncidenceRates(dataInc, dataPop, log = T)
	
dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])