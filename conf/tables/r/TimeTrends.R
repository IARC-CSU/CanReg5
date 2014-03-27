############################################################################################################
## TIME TRENDS
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
	source(paste(sep="/", script.basename, "Funct_graphs.R"))  # Graphic functions
	
## Filename & File type
	out <- checkArgs(Args, "-out")
	# This is in case the filename already contains .pdf, .svg or .png
	if(substr(out,nchar(out)-3,nchar(out)) %in% c(".csv",".pdf",".txt")){out <- substr(out,1,nchar(out)-4)}
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

## Getting the list of age groups to analyze
	groups <- checkArgs(Args, "-agegroup")
	groups <- strsplit(groups,"-")[[1]]
  agerange <- c(groups[1]:groups[2])

## Restricting incidence dataset
  dataInc <- dataInc[which(dataInc$AGE_GROUP>=as.integer(groups[1]) & dataInc$AGE_GROUP<=as.integer(groups[2])),]	
	
## Getting age group labels 
  agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])

## Processing standard population
  standpop <- GetStandPop(dataPop,agegroups=agerange)  

## Calculating ASR
	data <- CalcASR(dataInc, dataPop, standpop, strat=c("YEAR", "SEX"))
	
## Rounding results
	data$asr <- format( round(data$asr,2), format='f', digits=2)
	data$se <- format( round(data$se,2), format='f', digits=2)
	
      ## ------------------------------------------------
      ## Cases of unknown age
			unk_males <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==1),])
			unk_females <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==2),])
      ## ------------------------------------------------

	
## Preparing data the plot (adding labels etc)
	males <- data[data$SEX==1,]
	mlabs <- GetSiteLabels(dataInc,1)
	dataM <- merge(males,mlabs,by=c("ICD10GROUP"), sort=F)
		
	females <- data[data$SEX==2,]
	flabs <- GetSiteLabels(dataInc,2)
	dataF <- merge(females,flabs,by=c("ICD10GROUP"), sort=F)
	
	
## Getting the top X cancer sites for the whole period	
	number <- as.numeric(checkArgs(Args, "-number"))

	# Calculate ASR for men for the whole period
		tempM <- CalcASR(dataInc[which(dataInc$SEX==1),], dataPop, standpop, strat=c("SEX"))
	# Sorting by ASR
		tempM <- tempM[order(-tempM$asr), ]
	# Keeping only those sites that are in the data to be plotted
		tempM <- tempM[which(tempM$ICD10GROUP %in% dataM$ICD10GROUP),]
	# Top cancer sites
		TopMen <- head(tempM,number)
		TopMen <- TopMen$ICD10GROUP
	
	# Calculate ASR for men for the whole period
		tempF <- CalcASR(dataInc[which(dataInc$SEX==2),], dataPop, standpop, strat=c("SEX"))
	# Sorting by ASR
		tempF <- tempF[order(-tempF$asr), ]
	# Keeping only those sites that are in the data to be plotted
		tempF <- tempF[which(tempF$ICD10GROUP %in% dataF$ICD10GROUP),]
	# Top cancer sites
		TopWomen <- head(tempF,number)
		TopWomen <- TopWomen$ICD10GROUP	
	
	# Restricting data	
		dataM <- dataM[which(dataM$ICD10GROUP %in% TopMen),]
		dataF <- dataF[which(dataF$ICD10GROUP %in% TopWomen),]	
	
	
	
## If the file type is a figure
	if(plotTables==FALSE){

		# Processing male data
			dataM <- dataM[,c("ICD10GROUPLABEL","SEX","YEAR","asr","se","ICD10GROUP")]
			colnames(dataM) <- c("SITE","SEX","YEAR","ASR","SE","ICD10")
			dataM$SITE <- paste(dataM$SITE," (",dataM$ICD10,")",sep="")
	
		# Processing female data
			dataF <- dataF[,c("ICD10GROUPLABEL","SEX","YEAR","asr","se","ICD10GROUP")]
			colnames(dataF) <- c("SITE","SEX","YEAR","ASR","SE","ICD10")
			dataF$SITE <- paste(dataF$SITE," (",dataF$ICD10,")",sep="")

	
		## Getting the scale (log or not), labels and smoothing
			logr <- checkArgs(Args, "-logr")	
			smooth <- checkArgs(Args, "-smooth")
			label <- checkArgs(Args, "-label")
			header <- checkArgs(Args, "-header")
		
		## Plot for males
			filename1 <- paste(out,"-Males.", fileType, sep = "" )
			if(fileType=="png"){png(filename1)}
			if(fileType=="svg"){svg(filename1)}
			if(fileType=="pdf"){pdf(filename1, width=7)}
			graphM <- plotTimeTrends(dataM, logr, smooth, header, label, number)
			print(graphM)
			dev.off()
						
		## Plot for females
			filename2 <- paste(out,"-Females.", fileType, sep = "" )
			if(fileType=="png"){png(filename2)}
			if(fileType=="svg"){svg(filename2)}
			if(fileType=="pdf"){pdf(filename2, width=7)}
			graphF <- plotTimeTrends(dataF, logr, smooth, header, label, number)
			print(graphF)
			dev.off()
			
		## This is used by CanReg to open the files that were just created	
			cat(paste("-outFile",filename1,sep=":"))
			cat("\n")
			cat(paste("-outFile",filename2,sep=":"))			
	
		
	
	}else{
	
		# If not a table, we create a CSV table file	
		filename1 <- paste(out,"-Males.", fileType, sep = "" )
		write.table(dataM, filename1, sep = ",", row.names = F) 
		
		filename2 <- paste(out,"-Females.", fileType, sep = "" )
		write.table(dataF, filename2, sep = ",", row.names = F) 
		
		cat(paste("-outFile",filename1,sep=":"))
		cat("\n")
		cat(paste("-outFile",filename2,sep=":"))		
		
	}


		
	
	
	
	
	
	
	