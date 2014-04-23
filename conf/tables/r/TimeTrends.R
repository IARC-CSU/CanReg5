###############################################################################
## TIME TRENDS ANALYSIS
## for CanReg 5
## 
## Author: Sebastien Antoni
## Last update: 23/04/2014
###############################################################################



############################ ARGUMENTS AND OPTIONS ############################ 

## LIST OF ARGUMENTS FROM THE COMMAND LINE (CANREG)
    Args <- commandArgs(TRUE)

## SCRIPT DIRECTORY
    initial.options <- commandArgs(trailingOnly = FALSE)
    file.arg.name <- "--file="
    script.name <- sub(file.arg.name, "", 
                       initial.options[grep(file.arg.name, initial.options)])
    script.basename <- dirname(script.name)

## LOADING DEPENDENCIES
	source(paste(sep="/", script.basename, "checkArgs.R")) 
	source(paste(sep="/", script.basename, "Funct_rates.R"))  
	source(paste(sep="/", script.basename, "Funct_misc.R"))  
	source(paste(sep="/", script.basename, "Funct_graphs.R"))  

## OUTPUT FILE NAME AND TYPE
    out <- checkArgs(Args, "-out")
    if(substr(out,nchar(out)-3,nchar(out)) %in% c(".csv",".pdf",".txt")){
        out <- substr(out,1,nchar(out)-4)}

    fileType <- checkArgs(Args, "-ft")
	if(fileType %in% c("csv")){plotTables <- TRUE}else{plotTables <- FALSE}	

## NUMBER OF TOP CANCERS TO INCLUDE IN ANALYSES
    number <- as.numeric(checkArgs(Args, "-number"))

## RANGE OF AGE GROUPS TO INCLUDE IN ANALYSES
    groups <- checkArgs(Args, "-agegroup")
    groups <- strsplit(groups,"-")[[1]]
    agerange <- c(groups[1]:groups[2])

## GRAPHICS ARGUMENTS
    logr <- checkArgs(Args, "-logr")        # Log scale or not
    smooth <- checkArgs(Args, "-smooth")    # Use smoothing or not
    label <- checkArgs(Args, "-label")
    header <- checkArgs(Args, "-header")



############################### FORMATING DATA ############################### 

## INCIDENCE DATA
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
    
    # Restricting to selected age groups
    dataInc <- dataInc[which(dataInc$AGE_GROUP>=as.integer(groups[1]) & 
                                 dataInc$AGE_GROUP<=as.integer(groups[2])),]    

## POPULATION DATA
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	

    # Getting age group labels 
    agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])

## STANDARD POPULATION (From Population Data)
    standpop <- GetStandPop(dataPop,agegroups=agerange)  



################################## ANALYSIS ################################## 

## CALCULATING ASR
	data <- CalcASR(dataInc, dataPop, standpop, strat=c("YEAR", "SEX"))
	
    # Rounding results
	data$asr <- format( round(data$asr,2), format='f', digits=2)
	data$se <- format( round(data$se,2), format='f', digits=2)

    # Cases of unknown age (not used as of 24/04/2014)
    unk_males <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==1),])
    unk_females <- nrow(dataInc[which(dataInc$AGE_GROUP==19 & dataInc$SEX==2),])

    # Split by sex and Add site labels
    males <- data[data$SEX==1,]
    mlabs <- GetSiteLabels(dataInc,1)
    dataM <- merge(males,mlabs,by=c("ICD10GROUP"), sort=F)

    females <- data[data$SEX==2,]
    flabs <- GetSiteLabels(dataInc,2)
    dataF <- merge(females,flabs,by=c("ICD10GROUP"), sort=F)

    data <- rbind(dataM, dataF)

############################ PREPARATION OF OUTPUT ############################# 

    # This gets the top cancer sites for the whole period and by sex
    for(sex in 1:2){
        
        temp <- CalcASR(dataInc[which(dataInc$SEX==sex),], dataPop, standpop, 
                       strat=c("SEX"))
        temp <- temp[order(-temp$asr), ]
        temp <- temp[which(temp$ICD10GROUP %in% data$ICD10GROUP 
                          & temp$SEX==sex),]
       
        if(sex==1){TopMen <- head(temp,number)$ICD10GROUP}
        if(sex==2){TopWomen <- head(temp,number)$ICD10GROUP}
    }

    # Restricting data by sex to the top cancer sites
		dataM <- dataM[which(dataM$ICD10GROUP %in% TopMen),]
		dataF <- dataF[which(dataF$ICD10GROUP %in% TopWomen),]	
        data <- rbind(dataM, dataF)   
	

############################ GENERATION OF OUTPUT ############################# 

## FIGURE OUTPUT
    if(plotTables==FALSE){

        # Generating Graph for Each sex
        for(sex in 1:2){

            # Restricting to sex and specific columns
            dataOut <- data[data$SEX==sex,]
            dataOut <- dataOut[,c("ICD10GROUPLABEL","SEX","YEAR","asr","se",
                                  "ICD10GROUP")]
            colnames(dataOut) <- c("SITE","SEX","YEAR","ASR","SE","ICD10")
            dataOut$SITE <- paste(dataOut$SITE," (",dataOut$ICD10,")",sep="")
            
            # Output filename
            if(sex==1){fileOut <- paste(out,"-Males.", fileType, sep = "" )}
            if(sex==2){fileOut <- paste(out,"-Females.", fileType, sep = "" )}
            
            # Starting graph
            if(fileType=="png"){png(fileOut)}
            if(fileType=="svg"){svg(fileOut)}
            if(fileType=="pdf"){pdf(fileOut, width=7)}
            
            # Plotting
            graph <- plotTimeTrends(dataOut, logr, smooth, 
                                    header, label, number)
            print(graph)
            dev.off()
        }
			
		# Sending files to the console
		    cat(paste("-outFile:",out,"-Males.", fileType, sep = "" ))
		    cat("\n")
            cat(paste("-outFile:",out,"-Females.", fileType, sep = "" ))
		
# TABLE OUTPUT
    }else{
		
        fileOut <- paste(out,fileType, sep = "." )
    	write.table(data, fileOut, sep = ",", row.names = F) 
			
		cat(paste("-outFile",fileOut,sep=":"))
		cat("\n")
    }
