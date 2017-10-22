###############################################################################
## AGE SPECIFIC RATES, TOP X CANCERS 
## PRESENTED ON LINE CHARTS
## (for CanReg 5)
## 
## Author: Sebastien Antoni
## Last update: 25/04/2014
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
    dataPop <- read.table(filePop, header=TRUE)    

## LOADING STANDARD POPULATION (to compute the most common cancers)
    standpop <- GetStandPop(dataPop)    
 


################################## ANALYSIS ################################### 

## CALCULATING AGE SPECIFIC RATES
    data <- CalcAgeSpecRates(dataInc, dataPop)
    
## GETTING LIST OF TOP CANCERS IN MEN AND WOMEN
    datatop <- CalcASR(dataInc, dataPop, standpop, strat=c("SEX"))
    topcancers <- GetTopCancers(datatop) 

## RESTRICTING OUR DATASET TO TOP CANCERS
    data <- merge(data,topcancers, by=c("ICD10GROUP","SEX"), sort=F)



############################ PREPARATION OF OUTPUT ############################ 

## FIXING UP ICD LABELS
    data$ICD10GROUPLABEL <- substr(data$ICD10GROUPLABEL,4, 
                                   nchar(as.character(data$ICD10GROUPLABEL)))

## GETTING AGE GROUP LABELS
    agegrs <- GetAgeGroupsLabels(dataPop)
    #data <- merge(data,agegrs, by=c("AGE_GROUP"), sort=F

## ADDING MISSING VALUES
    data <- GenMissingAgeGrpData(data)



############################ GENERATION OF OUTPUT ############################# 

## PLOT OR TABLE
	if(plotTables==FALSE){
		
	    # Here we generate a graph for each sex
	    for(sex in unique(data$SEX)){
	        
            # Preparing dataset
	        dataOut <- data[data$SEX==sex,]
	        
	        # Output filename
	        if(sex==1){filename <- paste(out,"-Males.", fileType, sep = "" )}
	        if(sex==2){filename <- paste(out,"-Females.", fileType, sep = "" )}
	        if(sex==3){filename <- paste(out,"-Both.", fileType, sep = "" )}
	        
	        # Plotting
	        StartGraph(filename,fileType, height=5, width=5 )  # Starting graph
	        graph <- plotAgeSpecRates(dataOut, logr, smooth, header, label, 
                                      number, agegrs, color)	        
            print(graph)                      
	        dev.off()      
	    }
	    
	    # Opening files
	    cat(paste("-outFile:",out,"-Males.", fileType, sep = "" ))
	    cat("\n")
	    cat(paste("-outFile:",out,"-Females.", fileType, sep = "" ))
						
	}else{
	
		filename <- paste(out, fileType, sep = "." )
		write.table(data, filename, sep = ",", row.names = F) 
		cat(paste("-outFile",filename,sep=":"))
		
	}
