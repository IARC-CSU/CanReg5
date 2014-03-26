############################################################################################################
## GETS ASR IN TOP CANCERS AND PRESENT RESULTS ON 5 BAR CHART FOR BOTH SEXES
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
	source(paste(sep="/", script.basename, "Funct_graphs.R"))  # Graphics functions
	source(paste(sep="/", script.basename, "Funct_misc.R"))  # Misc functions
	
# Dependencies for reshape
	if(!is.installed("reshape2")){
		load.fun("reshape2")
	}       
	require(reshape2) 	
	
# Number of cancers to include
	number <- checkArgs(Args, "-number")
	
# Labels and headers	
	label <- checkArgs(Args, "-label")		
	header <- checkArgs(Args, "-header")	
	
## Filename & File type
	out <- checkArgs(Args, "-out")
	# This is in case the filename already contains .pdf, .svg or .png
	if(substr(out,nchar(out)-3,nchar(out)) %in% c(".csv",".pdf")){out <- substr(out,1,nchar(out)-4)}
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
		
## Getting POPULATION data
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	
	
## Getting age group labels
	standpop <- unique(dataPop[,c("AGE_GROUP","REFERENCE_COUNT")])
	standpop$REFERENCE_COUNT <- standpop$REFERENCE_COUNT*100
	
## Calculating ASR
	data <- CalcASR(dataInc, dataPop, standpop)	
	data$asr <- format( round(data$asr,2), format='f', digits=2)
	data$se <- format( round(data$se,2), format='f', digits=2)	
	
# Casting dataframe
	data <- data[,c("ICD10GROUP","SEX","asr")]
	data$asr <- as.numeric(data$asr)
	data <- dcast(data, ICD10GROUP~SEX, sum )
	colnames(data)<- c("ICD10GROUP","ASR_M","ASR_F")
				
# Adding labels
	labels <- unique(dataInc[,c("ICD10GROUP","ICD10GROUPLABEL")])
	labels <- labels[which(substr(labels$ICD10GROUPLABEL,1,1)==1 | substr(labels$ICD10GROUPLABEL,2,2)==1),]
	labels$ICD10GROUPLABEL <- substr(labels$ICD10GROUPLABEL,4,nchar(as.character(labels$ICD10GROUPLABEL)))
	data <- merge(data,labels,by=c("ICD10GROUP"), sort=F)
				
# Ordering dataset
	data$ASR_M <- as.numeric(data$ASR_M)
	data$ASR_F <- as.numeric(data$ASR_F)
	data$both <- data$ASR_M+data$ASR_F
	data <- data[order(data$both, decreasing = T), ]	
	data <- data[,c("ICD10GROUP","ICD10GROUPLABEL","ASR_M","ASR_F")]
	
	
## If the file type is a figure (not used here)
	if(plotTables==FALSE){
		
		# Plotting the ASR Pyramid
		filename <- paste(out, fileType, sep = "." )
		if(fileType=="png"){png(filename)}
		if(fileType=="svg"){jpeg(filename)}
		if(fileType=="pdf"){pdf(filename,height=5 ,width=7)}
		graph <- plotASRPyramid(data, header, label, number)   
		dev.off()
			
		## This is used by CanReg to open the files that were just created	
		cat(paste("-outFile",filename,sep=":"))
		
	}else{
	
	## If a table, we create a CSV table file	

		# Creating CSV file
		filename <- paste(out,".",fileType, sep = "" )
		write.table(data, filename, sep = ",", row.names = F) 
		
		## This is used by CanReg to open the files that were just created	
		cat(paste("-outFile",filename,sep=":"))		
		
	}



	
	
	
	
	
	
	