###############################################################################
## POPULATION PYRAMID 
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
   


############################### FORMATING DATA ############################### 

## LOADING POPULATION DATA
    dataPop <- read.table(filePop, header=TRUE)    
    dataPop <- dataPop[,c("YEAR","AGE_GROUP_LABEL","AGE_GROUP","SEX","COUNT")]



################################## ANALYSIS ################################## 

# Label for all years together
    periodlabel <- paste(min(dataPop$YEAR),"-",max(dataPop$YEAR), sep="")

# Aggregating data
    dataPop <- aggregate(dataPop$COUNT, by=list(dataPop$AGE_GROUP_LABEL,
                                            dataPop$AGE_GROUP,dataPop$SEX), 
                         FUN=mean)
    colnames(dataPop) <- c("AGE_GROUP_LABEL","AGE_GROUP" ,"SEX", "COUNT")

# Formatting results
    dataPop$COUNT <- round(dataPop$COUNT,0)	
    dataPop$YEAR <- periodlabel	


############################ GENERATION OF OUTPUT ############################# 

## FILENAME
    filename <- paste(out, fileType, sep = "." )

## PLOT OR TABLE
    if(plotTables==FALSE){

        # Plotting
        StartGraph(filename,fileType, height=5, width=7 )      # Starting graph
        graph <- plotPopulationPyramid(dataPop, header, label, showvals, color)
        dev.off()           
	
	}else{
		
		# Writting CSV file
		write.table(dataPop, filename, sep = ",", row.names = F) 
		
	}

# OPEN OUTPUT FILE
    cat(paste("-outFile",filename,sep=":"))

	
	
	