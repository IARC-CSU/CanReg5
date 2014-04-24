###############################################################################
## AGE STANDARDIZED RATES (ASR), TOP X CANCERS 
## PRESENTED ON A TWO SIDED BARCHART
## (for CanReg 5)
## 
## Author: Sebastien Antoni
## Last update: 24/04/2014
###############################################################################



############################ ARGUMENTS AND OPTIONS ############################ 

## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG) + SCRIPT DIRECTORY
    Args <- commandArgs(TRUE)
    initial.options <- commandArgs(trailingOnly = FALSE)
    file.arg.name <- "--file="
    script.name <- sub(file.arg.name, "", 
                        initial.options[grep(file.arg.name, initial.options)])
    script.basename <- dirname(script.name)

## LOADING DEPENDENCIES
    source(paste(sep="/", script.basename, "StartUp.R")) 
    if(!is.installed("reshape2")){load.fun("reshape2")}      
    require(reshape2) 



############################### FORMATING DATA ############################### 

## LOADING INCIDENCE AND POPULATION DATA
	dataInc <- read.table(fileInc, header=TRUE)
    dataInc <- dataInc[which(dataInc$AGE_GROUP>=as.integer(agegroups[1]) & 
                  dataInc$AGE_GROUP<=as.integer(agegroups[length(agegroups)])),]
    dataPop <- read.table(filePop, header=TRUE)	

## LOADING STANDARD POPULATION (For selected age groups, if relevant)
    standpop <- GetStandPop(dataPop,agegroups=agegroups)    



################################## ANALYSIS ################################## 

## CALCULATING ASR
	data <- CalcASR(dataInc, dataPop, standpop)	
	data$asr <- format(data$asr, format='f', digits=2)
	data$se <- format(data$se, format='f', digits=2)	



############################ PREPARATION OF OUTPUT ############################# 

# PREPARATION OF DATAFRAME
    data <- data[,c("ICD10GROUP","SEX","asr")]
    data$asr <- as.numeric(data$asr)
    data <- dcast(data, ICD10GROUP~SEX, sum )
    colnames(data)<- c("ICD10GROUP","ASR_M","ASR_F")

## ADDING LABELS
    labels <- GetICDLabels(dataInc)
    labels <- unique(labels[,c("ICD10GROUP","ICD10GROUPLABEL")])
    data <- merge(data,labels,by=c("ICD10GROUP"), sort=F)

## SORTING DATAFRAME
	data$both <- as.numeric(data$ASR_M)+as.numeric(data$ASR_F)
	data <- data[order(data$both, decreasing = T), ]	
	data <- data[,c("ICD10GROUP","ICD10GROUPLABEL","ASR_M","ASR_F")]
	


############################ GENERATION OF OUTPUT ############################# 

## FILENAME
    filename <- paste(out, fileType, sep = "." )

## PLOT OR TABLE
	if(plotTables==FALSE){

        StartGraph(filename,fileType, height=5, width=7 )      # Starting graph
        graph <- plotASRPyramid(data, header, label, number)   # Adding plot
		dev.off()                                              # Closing graph
        
	}else{
		
        write.table(data, filename, sep = ",", row.names = F) 

	}

# OPEN OUTPUT FILE
    cat(paste("-outFile",filename,sep=":"))
