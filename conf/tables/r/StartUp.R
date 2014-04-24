###############################################################################
## R STARTUP SCRIPT
## for CanReg 5
## 
## Author: Sebastien Antoni
## Last update: 24/04/2014
##
## Notes:
## ------
## This script loads all arguments passed to the command line, all the files
## containing functions etc.
##
## It should be loaded by all R Scripts used by CanReg and modified with 
## extreme care !!
##
###############################################################################

############################### LIBRARIES #####################################

## CHECK ARGS
    source(paste(sep="/", script.basename, "checkArgs.R")) 

## MISC FUNCTIONS
    source(paste(sep="/", script.basename, "Funct_misc.R"))  

## GRAPHICS-RELATED FUNCTIONS
    source(paste(sep="/", script.basename, "Funct_graphs.R"))  

## ANALYSIS RELATED FUNCTIONS
    source(paste(sep="/", script.basename, "Funct_rates.R"))  

###############################################################################



############################### ARGUMENTS #####################################

## GRAPHIC RELATED ARGUMENTS
logr <- checkArgs(Args, "-logr")        # Log scale or not
smooth <- checkArgs(Args, "-smooth")    # Use smoothing or not
label <- checkArgs(Args, "-label")      # Label
header <- checkArgs(Args, "-header")    # Header

## MISC ARGUMENTS
number <- checkArgs(Args, "-number")    # Number of cancers to be included
agegroups <- checkArgs(Args, "-agegroup") # age groups to be analysed


## OUTPUT FILE NAME 
out <- checkArgs(Args, "-out")
if(substr(out,nchar(out)-3,nchar(out)) %in% c(".svg",".csv",".pdf",".png")){
    out <- substr(out,1,nchar(out)-4)
}
if(substr(out,nchar(out)-4,nchar(out)) %in% c(".html")){
    out <- substr(out,1,nchar(out)-5)
}

## OUTPUT FILE TYPE
fileType <- checkArgs(Args, "-ft")  

## DATA FILES
fileInc <- checkArgs(Args, "-inc")
filePop <- checkArgs(Args, "-pop")



######################### OTHER GENERIC VARIABLES #############################

## OUTPUT TYPE (FIGURE OR TABLE)
if(fileType %in% c("csv")){plotTables <- TRUE}else{plotTables <- FALSE}	

## LIST OF AGE GROUPS TO INCLUDE IN ANALYSIS
agegroups <- strsplit(agegroups,"-")[[1]]
agegroups <- c(agegroups[1]:agegroups[2])
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    