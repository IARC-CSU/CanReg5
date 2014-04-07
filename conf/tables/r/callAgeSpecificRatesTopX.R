############################################################################################################
## AGE-SPECIFIC RATES
## This calls calculations and plotting commands to get age-specific rates of the top x cancers by site
############################################################################################################

## Catching the arguments sent to the script by CanReg
	Args <- commandArgs(TRUE)
		
## Directory of the script
	initial.options <- commandArgs(trailingOnly = FALSE)
	file.arg.name <- "--file="
	script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
	script.basename <- dirname(script.name)

## Loading dependencies
	source(paste(sep="/", script.basename, "checkArgs.R")) # Apparently this returns the arguments
	source(paste(sep="/", script.basename, "Funct_rates.R"))  # Rates calculation functions
	source(paste(sep="/", script.basename, "Funct_misc.R"))  # Misc functions
	source(paste(sep="/", script.basename, "Funct_graphs.R"))  # Graphics generating functions

## Getting the arguments from command line
  logr <- checkArgs(Args, "-logr")	
  smooth <- checkArgs(Args, "-smooth")
  label <- checkArgs(Args, "-label")
  header <- checkArgs(Args, "-header")
  color <- checkArgs(Args, "-color")

## Filename & File type
	out <- checkArgs(Args, "-out")
	# This is in case the filename already contains .pdf, .svg or .png
	if(substr(out,nchar(out)-3,nchar(out)) %in% c(".png",".pdf",".svg")){out <- substr(out,1,nchar(out)-4)}
	fileType <- checkArgs(Args, "-ft")
	
## Is the output file a table or a picture		
	if(fileType %in% c("csv")){
		plotTables <- TRUE
	}else{
		plotTables <- FALSE
	}	
	
## Getting and Formatting INCIDENCE data
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
	dataInc$ICD10GROUPLABEL <- substr(dataInc$ICD10GROUPLABEL,4,length(dataInc$ICD10GROUPLABEL))
  
## Getting POPULATION data
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	

## Getting age group labels
	agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])
	standpop <- unique(dataPop[,c("AGE_GROUP","REFERENCE_COUNT")])
	standpop$REFERENCE_COUNT <- standpop$REFERENCE_COUNT*100
	
## Calculating Age Specific rates for all sites
	data <- CalcAgeSpecRates(dataInc, dataPop)	

## Calculating ASR for all sites (sorted) 
  dataCR <- CalcASR(dataInc,dataPop,standpop)  
  dataCR <- dataCR[order(dataCR$SEX, -dataCR$asr), ]

## Getting the top X sites by sex (change once implemented)
	number <- as.numeric(checkArgs(Args, "-number"))
	
	# Males
	males <- head(dataCR[dataCR$SEX==1,],number)
	males <- males[,c("SEX","ICD10GROUP")]
	
	# Females
	females <- head(dataCR[dataCR$SEX==2,],number)
	females <- females[,c("SEX","ICD10GROUP")]

## Restricting our dataset to the sites selected	
	dataM <- merge(data,males,by=c("SEX","ICD10GROUP"))
	dataF <- merge(data,females,by=c("SEX","ICD10GROUP"))
	data <- rbind(dataF,dataM)

## Adding missing data 
    dataM <- GenMissingAgeGrpData(dataM)
    dataF <- GenMissingAgeGrpData(dataF)

## If the file type is a figure
	if(plotTables==FALSE){
		
		## Plot for males
			filename1 <- paste(out,"-Males.", fileType, sep = "" )
			if(fileType=="png"){png(filename1)}
			if(fileType=="svg"){svg(filename1)}
			if(fileType=="pdf"){pdf(filename1, width=7)}
			graphM <- plotAgeSpecRates(dataM, logr, smooth, header, label, number, agegrs, color)
			print(graphM)
			dev.off()
						
		## Plot for females
			filename2 <- paste(out,"-Females.", fileType, sep = "" )
			if(fileType=="png"){png(filename2)}
			if(fileType=="svg"){svg(filename2)}
			if(fileType=="pdf"){pdf(filename2, width=7)}
			graphF <- plotAgeSpecRates(dataF, logr, smooth, header, label, number, agegrs, color)
			print(graphF)
			dev.off()
			
		## This is used by CanReg to open the files that were just created	
			cat(paste("-outFile",filename1,sep=":"))
			cat("\n")
			cat(paste("-outFile",filename2,sep=":"))		
						
	}else{
	
		# If not a table, we create a CSV table file	
		filename <- paste(out, fileType, sep = "." )
		write.table(data, filename, sep = ",", row.names = F) 
		
		cat(paste("-outFile",filename,sep=":"))
		
	}

		


