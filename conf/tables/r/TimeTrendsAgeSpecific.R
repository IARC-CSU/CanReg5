###############################################################################
## AGE SPECIFIC TREND ANALYSIS
## for CanReg 5
## 
## Author: Sebastien Antoni
## Last update: 10/06/2014
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

## INCIDENCE DATA
	fileInc <- checkArgs(Args, "-inc")
	dataInc <- read.table(fileInc, header=TRUE)
      
      # Removing Non-Melanoma Skin Cancer
      dataInc <- dataInc[dataInc$ICD10GROUP!="C44",]      

      # Setting all ICDs to All sites
      dataInc$ICD10GROUP <- "AllbutC44"
      dataInc$ICD10GROUPLABEL <- "All Sites but NMSC"

      # Restricting to age groups > 40
      dataInc <- dataInc[dataInc$AGE_GROUP >= 8,]

## POPULATION DATA
      filePop <- checkArgs(Args, "-pop")
      dataPop <- read.table(filePop, header=TRUE)	

      # Labels for age groups
      agegrs <- unique(dataPop[,c("AGE_GROUP","AGE_GROUP_LABEL")])


################################## ANALYSIS #################################### 

# CALCULATING AGE SPECIFIC RATES
      data <- CalcAgeSpecRates(dataInc, dataPop, strat=c("YEAR", "SEX"))

      data <- merge(data, agegrs, by=c("AGE_GROUP"))
      data$AGE_GROUP <- data$AGE_GROUP_LABEL

#################################### OUTPUT #################################### 


if(plotTables==FALSE){

      
      
      for(sex in 1:2){
            
            # Output Filename
            if(sex==1){fileOut <- paste(out,"-Males.", fileType, sep = "" )}
            if(sex==2){fileOut <- paste(out,"-Females.", fileType, sep = "" )}
            
            # Starting graph
            
            if(fileType=="pdf"){pdf(fileOut, width=7)}
            
            # Generating Graph (on a new page) for each site
            for(site in unique(data$ICD10GROUP)){
                  
                  # Restricting dataset
                  dataOut <- data[which(data$SEX==sex & data$ICD10GROUP==site),]
                  
                  if(nrow(dataOut)>=1){
                        dataOut$SITE <- paste(dataOut$ICD10GROUPLABEL," (",dataOut$ICD10GROUP,")", sep="")
                        
                        # Plotting
                        graph <- plotAgeSpecificTrends(dataOut, logr, smooth, header, label)
                        print(graph)
              
                  }            
                  
            }
            
            dev.off()
            cat(paste("-outFile",fileOut,sep=":"))
            cat("\n")
      }
      
      
      
      
}else{
      
      # Output filename
      fileOut <- paste(out,fileType, sep = "." )
      
      # Exporting
      write.table(data, fileOut, sep = ",", row.names = F) 
      
      # Sending file to console
      cat(paste("-outFile",fileOut,sep=":"))
      cat("\n")
}




