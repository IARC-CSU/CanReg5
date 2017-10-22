############################################################################################################
## DATA QUALITY INDICATORS
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
	
## Getting age group labels
	agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])
  standpop <- GetStandPop(dataPop)
	
## Adding data for All Sites
	dataAll <- dataInc[which(dataInc$ICD10GROUP!="C44"),]			
	dataAll$ICD10GROUP <- "ALLbC44"
	dataAll$ICD10GROUPLABEL <- "11 All sites but C44"
	dataAll <- aggregate(dataAll$CASES, by=list(dataAll$YEAR,dataAll$ICD10GROUP,dataAll$ICD10GROUPLABEL,dataAll$SEX,dataAll$AGE_GROUP, dataAll$MORPHOLOGY ,dataAll$BEHAVIOUR,dataAll$BASIS), FUN=sum)
	colnames(dataAll) <- c("YEAR", "ICD10GROUP","ICD10GROUPLABEL" ,"SEX", "AGE_GROUP","MORPHOLOGY","BEHAVIOUR" ,"BASIS","CASES")
	dataInc <- rbind(dataInc,dataAll)
	
## Calculating ASR
	data <- CalcASR(dataInc, dataPop, standpop)
	
##	Getting basis of diagnosis distribution
   #dataB <- GetBasisDist(dataInc)

          ## ------------------------------------------------
          # Removing cases of age unknown (TEMPORARY)
          dataB <- dataInc[which(dataInc$AGE_GROUP!=19),]
          dataB <- GetBasisDist(dataB)
          ## ------------------------------------------------	

## Merging data frames
	data <- merge(data,dataB,by=c("ICD10GROUP","SEX"), sort=F)	

## Calculating percentages
	data$"DCO(%)" <- round(data$"DCO(N)" * 100 / data$N,2)
	data$"CLIN(%)" <- round(data$"CLIN(N)" * 100 / data$N,2)
	data$"MV(%)" <- round(data$"MV(N)" * 100 / data$N,2)
	data$"UNK(%)" <- round(data$"UNK(N)" * 100 / data$N,2)
	
## Rounding results
  data$asr <- format( round(data$asr,2), format='f', digits=2)
  data$se <- format( round(data$se,2), format='f', digits=2)


      ## ------------------------------------------------
      ## Cases of unknown age
        unk_males <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==1),])
        unk_females <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==2),])
      ## ------------------------------------------------


## labels
	males <- data[data$SEX==1,]
	mlabs <- GetSiteLabels(dataInc,1)
	dataM <- merge(males,mlabs,by=c("ICD10GROUP"), sort=F)
	
	females <- data[data$SEX==2,]
	flabs <- GetSiteLabels(dataInc,2)
	dataF <- merge(females,flabs,by=c("ICD10GROUP"), sort=F)
	
	
## If the file type is a figure
	if(plotTables==FALSE){
		
		# filename for output  
	    filename <- paste(out,".", fileType, sep = "" ) 
    
    # Processing male data
      dataOutM <- dataM[,c("ICD10GROUPLABEL","N","asr","se","MV(%)","CLIN(%)","DCO(%)","ICD10GROUP")]
      dataOutM$ASR <- paste(dataOutM$asr," (",dataOutM$se,")",sep="")
      dataOutM$PERC <- format( round(dataOutM$N*100/sum(males$N[males$ICD10GROUP!="ALLbC44"]),2), format='f', digits=2) 
      dataOutM <- dataOutM[,c("ICD10GROUPLABEL","N","PERC","ASR","MV(%)","CLIN(%)","DCO(%)","ICD10GROUP")]
      colnames(dataOutM) <- c("SITE","Cases","% Total","ASR(se)","MV(%)","CLIN(%)","DCO(%)","ICD10")
    
    # Processing female data
      dataOutF <- dataF[,c("ICD10GROUPLABEL","N","asr","se","MV(%)","CLIN(%)","DCO(%)","ICD10GROUP")]
      dataOutF$ASR <- paste(dataOutF$asr," (",dataOutF$se,")",sep="")
      dataOutF$PERC <- format( round(dataOutF$N*100/sum(females$N[females$ICD10GROUP!="ALLbC44"]),2), format='f', digits=2) 
	  dataOutF <- dataOutF[,c("ICD10GROUPLABEL","N","PERC","ASR","MV(%)","CLIN(%)","DCO(%)","ICD10GROUP")]
      colnames(dataOutF) <- c("SITE","Cases","% Total","ASR(se)","MV(%)","CLIN(%)","DCO(%)","ICD10"  )  
        	
    # Checking that gplots is installed and if not, installs it and includes it
      if(!is.installed("gplots")){
        load.fun("gplots")
      }       
      require(gplots)    
    
    # Creating pdf 
      if(fileType=="pdf"){
        pdf(filename, width=8, height=11)
      }
   
    # Getting header
      header <- checkArgs(Args, "-header")
    
    # Graphical parameters
      par(mfrow=c(2,1))
      #par(mai=c(0.5,0.5,0.5,0.5))
      par(mar=c(1,1,1,1)) ## margin of a plot
      par(oma=c(1,1,6,1)) ## outer margin (of the whole plot series, not individual plots)
    
    # Male indicators
      print(textplot(dataOutM, valign="top", show.rownames=F, cex=0.8 ,cmar = 1, rmar=0.70, mar=c(1,1,2,1)))
      print(title("MALE"))
    
    # Main graphic title
      mtext(header,side=3,line=4, cex=1.3, font=2)
      mtext("Data Quality Indicators",side=3,line=3, cex=1)
    
    # Female indicators
      print(textplot(dataOutF, valign="top", show.rownames=F, cex=0.8 , cmar = 1, rmar=0.70, mar=c(1,1,2,1))) 
      print(title("FEMALE"))
    
      mtext(paste("Cases of unknown age (", unk_males," M / ",unk_females," F) were excluded from these analyses", sep=""),side=1,line=1, cex=0.8, font=1)
    
    # Finalizing and opening graph
      dev.off()	
      cat(paste("-outFile",filename,sep=":"))
    
						
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



	
	
	
	
	
	
	