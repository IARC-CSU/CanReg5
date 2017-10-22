###############################################################################
## TIME TRENDS ANALYSIS (ASR), TOP X CANCERS 
## PRESENTED ON LINE CHARTS
## (for CanReg 5)
## 
## Author: Sebastien Antoni
## Last update: 07/07/2016 by Morten Ervik
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
    data <- CalcASR(dataInc, dataPop, standpop, strat=c("YEAR", "SEX"))
    data$asr <- format(data$asr, format='f', digits=2)
    data$se <- format(data$se, format='f', digits=2)	

## GETTING LIST OF TOP CANCERS IN MEN AND WOMEN
    datatop <- CalcASR(dataInc, dataPop, standpop, strat=c("SEX"))
    topcancers <- GetTopCancers(datatop, number) 
  
## RESTRICTING OUR DATASET TO TOP CANCERS
    data <- merge(data,topcancers, by=c("ICD10GROUP","SEX"), sort=F)



############################ PREPARATION OF OUTPUT ############################# 

## ADDING LABELS
    labels <- GetICDLabels(dataInc)
    labels <- unique(labels[,c("ICD10GROUP","ICD10GROUPLABEL")])
    data <- merge(data,labels,by=c("ICD10GROUP"), sort=F)



############################ GENERATION OF OUTPUT ############################# 

## PLOT OR TABLE
if(plotTables==FALSE){

    # Here we generate a graph for each sex
    for(sex in unique(data$SEX)){
    
        # Preparing dataset
        dataOut <- data[data$SEX==sex,]
        dataOut <- dataOut[,c("ICD10GROUPLABEL","SEX","YEAR","asr","se",
                                  "ICD10GROUP")]
        dataOut$SITE <- 
            paste(dataOut$ICD10GROUPLABEL," (",dataOut$ICD10GROUP,")",sep="")
            
        # Output filename
        if(sex==1){filename <- paste(out,"-Males.", fileType, sep = "" )}
        if(sex==2){filename <- paste(out,"-Females.", fileType, sep = "" )}
        if(sex==3){filename <- paste(out,"-Both.", fileType, sep = "" )}
        
        # Plotting
        StartGraph(filename,fileType, height=5, width=7 )      # Starting graph
        graph <- plotTimeTrends(dataOut, logr, smooth, header, label, number)
        print(graph)                      
        dev.off()      
    }
			
	# Opening files
    cat(paste("-outFile:",out,"-Males.", fileType, sep = "" ))
	cat("\n")
    cat(paste("-outFile:",out,"-Females.", fileType, sep = "" ))
    cat("\n")	

}else{

    fileOut <- paste(out,fileType, sep = "." )
    write.table(data, fileOut, sep = ",", row.names = F) 
        
    cat(paste("-outFile",fileOut,sep=":"))
    cat("\n")
}
