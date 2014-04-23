###############################################################################
## AGE SPECIFIC TREND ANALYSIS
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

## GRAPHICS ARGUMENTS
    logr <- checkArgs(Args, "-logr")        # Log scale or not
    smooth <- checkArgs(Args, "-smooth")    # Use smoothing or not
    label <- checkArgs(Args, "-label")
    header <- checkArgs(Args, "-header")



############################### FORMATING DATA ############################### 

## INCIDENCE DATA
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)

        #dataInc <- dataInc[dataInc$ICD10GROUP=="ALLbC44",]      # DELETE
        #dataInc$ICD10GROUP <- "ALL"
        #dataInc$ICD10GROUPLABEL <- "All Sites"
        #dataInc <- dataInc[which(dataInc$MORPHOLOGY=="8170" | 
                             #dataInc$MORPHOLOGY=="8171"),]      # DELETE
        dataInc$AGE_GROUP[dataInc$AGE_GROUP %in% 0:5] <- "0-29"
        dataInc$AGE_GROUP[dataInc$AGE_GROUP %in% 6:12] <- "30-64"
        dataInc$AGE_GROUP[dataInc$AGE_GROUP %in% 13:17] <- "65+"

## POPULATION DATA
	filePop <- checkArgs(Args, "-pop")
	dataPop <- read.table(filePop, header=TRUE)	

        dataPop$AGE_GROUP[dataPop$AGE_GROUP %in% 0:5] <- "0-29" # DELETE
        dataPop$AGE_GROUP[dataPop$AGE_GROUP %in% 6:12] <- "30-64"
        dataPop$AGE_GROUP[dataPop$AGE_GROUP %in% 13:17] <- "65+"

    # Getting age group labels 
    agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])



################################## ANALYSIS ################################## 

## CALCULATING AGE SPECIFIC RATES
    data <- CalcAgeSpecRates(dataInc, dataPop, strat=c("YEAR", "SEX"))

    # Split by sex and Add site labels
    dataM <- data[data$SEX==1,]
    dataF <- data[data$SEX==2,]
    data <- rbind(dataM, dataF)



############################ PREPARATION OF OUTPUT ############################# 
    


############################ GENERATION OF OUTPUT ############################# 
 
## SELECTING COLUMNS TO EXPORT/USE
    data <- data[,c("ICD10GROUPLABEL","SEX","YEAR","AGE_GROUP",
                      "RATE","ICD10GROUP")]
    colnames(data) <- c("SITE","SEX","YEAR","AGE_GROUP","RATE","ICD10")
    
    data$SITE <- substr(data$SITE,4,1000) 

    # Generate missing data (0s)
    data <- GenMissingData(data,strat=c("SEX","SITE","ICD10","AGE_GROUP"),
                           longvar="YEAR",val="RATE")

## FIGURE OUTPUT
    if(plotTables==FALSE){
    
        # A graph for each sex
        for(sex in 1:2){
            
            # Output Filename
            if(sex==1){fileOut <- paste(out,"-Males.", fileType, sep = "" )}
            if(sex==2){fileOut <- paste(out,"-Females.", fileType, sep = "" )}
            
            # Starting graph
            #if(fileType=="png"){png(fileOut)}
            #if(fileType=="svg"){svg(fileOut)}
            if(fileType=="pdf"){pdf(fileOut, width=7)}
                        
            # Generating Graph (on a new page) for each site
            for(site in unique(data$ICD10)){
            
                # Restricting dataset
                dataOut <- data[which(data$SEX==sex & data$ICD10==site),]
                
                if(nrow(dataOut)>=1){
                dataOut$SITE <- paste(dataOut$SITE," (",dataOut$ICD10,")", sep="")
                                            
                # Plotting
                graph <- plotAgeSpecificTrends(dataOut, logr, smooth, header, label)
                print(graph)
                }            
                
            }
            
            dev.off()
            cat(paste("-outFile",fileOut,sep=":"))
            cat("\n")
        }
        

        
        
# TABLE OUTPUT
    }else{
    
        # Output filename
        fileOut <- paste(out,fileType, sep = "." )
        
        # Exporting
        write.table(data, fileOut, sep = ",", row.names = F) 
    
        # Sending file to console
        cat(paste("-outFile",fileOut,sep=":"))
        cat("\n")
    }



