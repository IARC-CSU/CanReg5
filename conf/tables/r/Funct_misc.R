
# Install a package		
	load.fun <- function(x) { 
		old.repos <- getOption("repos") 
		on.exit(options(repos = old.repos))               #this resets the repos option when the function exits 
		new.repos <- old.repos 
		new.repos["CRAN"] <- "http://cran.rstudio.com/"   #Rstudio, automatic redirection to servers worldwide , or set your favorite  CRAN Mirror here 
		options(repos = new.repos)
		x <- as.character(substitute(x))
		dir.create(Sys.getenv("R_LIBS_USER"), recursive = TRUE)
		rlibs = gsub("\\", "/", Sys.getenv("R_LIBS_USER"), fixed = T)
		eval(parse(text=paste("install.packages('", x, "', dep=TRUE, lib='", rlibs,"' )", sep=""))) 
		eval(parse(text=paste("require(", x, ", lib.loc = '", rlibs ,"')", sep="")))
		
	}

# Check that a package is installed		
	is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 


# Gets the labels for age groups
GetAgeGroupLabels <- function(lastGr){
	# All labels (for 18 age groups)
		label <- c("0-4","5-9","10-14","15-19","20-24","25-29","30-34","35-39","40-44","45-49","50-54","55-59","60-64","65-69","70-74","75-79","80-84","85+")
	
	# We restrict our vector to labels from1 to lastGr
		label <- label[1:lastGr]
	
	# Renaming the last group
		label[lastGr] <- paste(substr(label[lastGr],0,2),"+",sep="")
	
	# indexes
		AGE_GROUP <- c(1:lastGr)
	
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
			
		# Varifying that all columns exist
			if(!"9" %in% colnames(data)){data$"9"<-0}
			if(!"7" %in% colnames(data)){data$"7"<-0}
			if(!"1" %in% colnames(data)){data$"1"<-0}
			if(!"0" %in% colnames(data)){data$"0"<-0}
					
		return(data)
	}


	
# Get the labels from a data frame for a specific sex	
	GetSiteLabels <- function(dataInc, sex){
		# Keeping data for that sex only
		data <- dataInc[substr(dataInc$ICD10GROUPLABEL,sex,sex)==1,]
		data <- data[,c("ICD10GROUP","ICD10GROUPLABEL")]
		data <- unique(data)
		data$ICD10GROUPLABEL <- substr(data$ICD10GROUPLABEL,4,nchar(as.character(data$ICD10GROUPLABEL)))
		return(data)
	}

	


