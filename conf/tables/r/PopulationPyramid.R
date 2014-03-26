############################################################################################################
## Population Pyramid
##
##	By Sebastien ANTONI
##  Date: Mar 2014
##
############################################################################################################

# ----------------------------------------------------------------------------------------------------------
# Loading dependencies	& Getting the arguments from CanReg	
# ----------------------------------------------------------------------------------------------------------
	
	# CANREG BASIC ARGUMENTS
	Args <- commandArgs(TRUE)
	initial.options <- commandArgs(trailingOnly = FALSE)
	file.arg.name <- "--file="
	script.name <- sub(file.arg.name, "", initial.options[grep(file.arg.name, initial.options)])
	script.basename <- dirname(script.name)
		
	# DEPENDENCIES
	source(paste(sep="/", script.basename, "checkArgs.R")) # Apparently this returns the arguments
	source(paste(sep="/", script.basename, "Funct_graphs.R"))  # Graphic functions
	
	
	# OUTPUT FILE NAME & HEADER
	out <- checkArgs(Args, "-out")		
	if(substr(out,nchar(out)-3,nchar(out)) %in% c(".csv",".pdf",".txt")){out <- substr(out,1,nchar(out)-4)}
	fileType <- checkArgs(Args, "-ft")
	if(fileType %in% c("csv")){plotTables <- TRUE}else{plotTables <- FALSE}
	header <- checkArgs(Args, "-header")
	label <- checkArgs(Args, "-label")
	
	# DATA FILES
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	
	dataPop <- dataPop[,c("YEAR","AGE_GROUP_LABEL","AGE_GROUP","SEX","COUNT")]
		
	years <- paste(min(dataPop$YEAR),"-",max(dataPop$YEAR), sep="")
		
	# AGGREGATING DATA
	dataPop <- aggregate(dataPop$COUNT, by=list(dataPop$AGE_GROUP_LABEL,dataPop$AGE_GROUP,dataPop$SEX), FUN=mean)
	colnames(dataPop) <- c("AGE_GROUP_LABEL","AGE_GROUP" ,"SEX", "COUNT")
	dataPop$COUNT <- round(dataPop$COUNT,0)	
	dataPop$YEAR <- years	
	
	# GRAPHIC (IF RELEVANT)
	if(!plotTables){
							
		## Plot pyramid
			filename <- paste(out, fileType, sep = "." )
			if(fileType=="png"){png(filename)}
			if(fileType=="svg"){jpeg(filename)}
			if(fileType=="pdf"){pdf(filename, width=7)}
			graph <- plotPopulationPyramid(dataPop, header, label)   #, agegrs
			#print(graph)
			dev.off()
			
		## This is used by CanReg to open the files that were just created	
			cat(paste("-outFile",filename,sep=":"))
		
	
	}else{
		
		data <- dataPop
		
		# EXPORTING
		filename <- paste(out, fileType, sep = "." )
		write.table(data, filename, sep = ",", row.names = F) 
		cat(paste("-outFile",filename,sep=":"))
	}

		

		
	
	
	
	