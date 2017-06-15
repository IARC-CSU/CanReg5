
############################## NEW FUNCTIONS ############################## 

# Install a package		
	load.fun <- function(x) { 
		old.repos <- getOption("repos") 
		on.exit(options(repos = old.repos))               #this resets the repos option when the function exits 
		new.repos <- old.repos 
		new.repos["CRAN"] <- "http://cran.rstudio.com/"   #Rstudio, automatic redirection to servers worldwide , or set your favorite  CRAN Mirror here 
		options(repos = new.repos)
		x <- as.character(substitute(x))
                rlibs <- gsub("\\", "/",file.path(paste0(Sys.getenv("R_LIBS_USER"), "-CanReg5")), fixed = T)
                dir.create(rlibs, recursive = TRUE)
		eval(parse(text=paste("install.packages('", x, "', lib='", rlibs,"' )", sep=""))) 
		eval(parse(text=paste("require(", x, ", lib.loc = '", rlibs ,"')", sep="")))		
	}

# Check that a package is installed		
	is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

    
# Get the labels from a data frame for a specific sex    
	GetICDLabels <- function(data){
	    
	    # Unique combinations of ICD codes / labels
        labels <- unique(data[,c("ICD10GROUP","ICD10GROUPLABEL")])
	    
        # Males and females labels
        labelsM <- labels[substr(labels$ICD10GROUPLABEL,1,1)==1,]
        labelsM$SEX <- 1
        
        labelsF <- labels[substr(labels$ICD10GROUPLABEL,2,2)==1,]
        labelsF$SEX <- 2
        
        labelsBoth <- rbind(labelsM, labelsF)
        labelsBoth$SEX <- 3
        
        labels <- rbind(labelsM,labelsF,labelsBoth)
        
        # Removing leading values
        labels$ICD10GROUPLABEL <- 
            substr(labels$ICD10GROUPLABEL,4,
                   nchar(as.character(labels$ICD10GROUPLABEL)))
        	    
	    return(labels)
	}  
 

# Adapts the standard population to a certain age range 
GetStandPop <- function(dataPop,agegroups=c(0:17)){
    
    # Getting standpop from the population data
    standpop <- unique(dataPop[,c("AGE_GROUP","REFERENCE_COUNT")])
    standpop$REFERENCE_COUNT <- standpop$REFERENCE_COUNT*100
    
    # Calculating total standard population (normally 100,000)
    totalstandpop <- 
        sum(standpop$REFERENCE_COUNT[standpop$AGE_GROUP %in% agegroups])
    
    # Restricting to selected age groups
    standpop <- standpop[which(standpop$AGE_GROUP %in% agegroups),]
    
    # Updating the standard population
    standpop$REFERENCE_COUNT <- 
        standpop$REFERENCE_COUNT*100000/totalstandpop # Adj for missing ages
    
    # Returning new pop
    return(standpop)
}   




    
    
############################## OLD FUNCTIONS ############################## 

    
    
    
    
    
    
    
    
    
    
    
    

# Gets the labels for age groups
GetAgeGroupLabels <- function(lastGr){
	# All labels (for 18 age groups)
		label <- c("0-4","5-9","10-14","15-19","20-24","25-29","30-34","35-39","40-44","45-49","50-54","55-59","60-64","65-69","70-74","75-79","80-84","85+")
	
	# We restrict our vector to labels from1 to lastGr
		label <- label[1:lastGr]
	
	# Renaming the last group
		label[lastGr] <- paste(substr(label[lastGr],0,2),"+",sep="")
	
	# indexes
		AGE_GROUP <- c(0:lastGr)
	
	# Merging
		label <- as.data.frame(cbind(AGE_GROUP,label))
	
	return(label)

}


# Calculate the distribution of DCO, MV, and Clinical cases for each site
	GetBasisDist <- function(dataInc){
		
		if(!is.installed("reshape2")){
			load.fun("reshape2")
		}
		require(reshape2)
	    
		# Replacing basis of diagnosis codes
			dataInc$BASIS[dataInc$BASIS %in% c(1:4)] <- 1
			dataInc$BASIS[dataInc$BASIS %in% c(5:8)] <- 7
	
		# Getting the total number of cases by site and sex and basis of diag
			data <- as.data.frame(aggregate(dataInc$CASES,by=list(dataInc$ICD10GROUP,dataInc$SEX, dataInc$BASIS),FUN=sum, na.rm=TRUE))
			colnames(data) <- c("ICD10GROUP","SEX","BASIS","CASES")
		
		# Pivoting the data frame
			data <- dcast(data, ICD10GROUP+SEX~BASIS, sum, value.var= "CASES")
		
		# Verifying that all columns exist
			if(!"9" %in% colnames(data)){data$"9"<-0}
			if(!"7" %in% colnames(data)){data$"7"<-0}
			if(!"1" %in% colnames(data)){data$"1"<-0}
			if(!"0" %in% colnames(data)){data$"0"<-0}

		# Renaming columns
		data <- data[,c("ICD10GROUP","SEX","0","1","7","9")]	
    colnames(data) <- c("ICD10GROUP","SEX","DCO(N)","CLIN(N)","MV(N)","UNK(N)")
			
		return(data)
	}


	
# Get the labels from a data frame for a specific sex (Returns only those that are to be included)
	GetSiteLabels <- function(dataInc, sex){
		
		# Keeping data for that sex only
		if(sex==1 || sex==2){data <- dataInc[which(substr(dataInc$ICD10GROUPLABEL,sex,sex)==1),]}
		if(sex==3){data <- dataInc[which(substr(dataInc$ICD10GROUPLABEL,1,2)!="00"),]}
		
		data <- data[,c("ICD10GROUP","ICD10GROUPLABEL")]
		data <- unique(data)
		data$ICD10GROUPLABEL <- substr(data$ICD10GROUPLABEL,4,nchar(as.character(data$ICD10GROUPLABEL)))
		return(data)
	}

	
# Get the labels from a data frame for a specific sex	
	GetAllSiteLabels <- function(dataInc){
		data <- dataInc[,c("ICD10GROUP","ICD10GROUPLABEL")]
		data <- unique(data)
		data$ICD10GROUPLABEL <- substr(data$ICD10GROUPLABEL,4,nchar(as.character(data$ICD10GROUPLABEL)))
		return(data)
	}

  
  


	
  
# Generate missing age groups (TEMP function)   
# This is used to plot the age specific rates
#  it generates results for age groups without cases
    
  GenMissingAgeGrpData <- function(data){
    
    for(SEX in unique(data$SEX)){
      for(SITE in unique(data$ICD10GROUP[data$SEX==SEX])){
        
        # temporary subset of data
        temp <- data[which(data$ICD10GROUP==SITE & data$SEX==SEX),]
        
        # theorical age groups range
        agegrps <- c(min(temp$AGE_GROUP):max(temp$AGE_GROUP))
    
        # Label for the site
        misslabel <- temp$ICD10GROUPLABEL[1]
	    
	      # Ages that do not have any cases
	      missingAges <- setdiff(agegrps,unique(temp$AGE_GROUP))
	      	      
        # Generates the fake data
	      for(miss in missingAges){
	        temp <- data.frame(SEX=SEX,ICD10GROUP=SITE,AGE_GROUP=miss,ICD10GROUPLABEL=misslabel,
	                           CASES=0,COUNT=1,RATE=0.1,stringsAsFactors=FALSE)
	        data <- rbind(data,temp)
	      }
	    }
	  }
		return(data)   
	}
	
	
	
    
    
    
GenMissingData <- function(data, strat=c(""), longvar="",val="RATE"){
    
    # Strat are the variables on the left, longvar the one on the top
    # val, the one in the cells (to be generated if missing)
    if(!is.installed("reshape2")){
        load.fun("reshape2")
    }
    require(reshape2)
    
    # Casting data
    data <- dcast(data, eval(parse(text=paste(paste(strat, collapse="+"),"~",
                                    longvar,sep=""))), value.var=val)
   
    data[is.na(data)]<-0
    
    data <- melt(data, id=c(strat), variable.name=longvar, value.name=val)

    #write.csv(data, "c:/Users/antonis/Desktop/seb3.csv", row.names = F) 
    
    
     return(data)
}
	
    
    
    
	

