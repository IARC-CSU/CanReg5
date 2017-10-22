################################################################################
## CI5-LIKE EDITORIAL TABLES 
## (for CanReg 5)
## 
## Author: Sebastien Antoni
## Last update: 05/05/2014
################################################################################


############################ ARGUMENTS AND OPTIONS ############################# 

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



############################### FORMATING DATA ################################# 

## LOADING INCIDENCE AND POPULATION DATA
    dataInc <- read.table(fileInc, header=TRUE)
    dataPop <- read.table(filePop, header=TRUE)    

## LOADING STANDARD POPULATION (to compute the most common cancers)
    standpop <- GetStandPop(dataPop) 

## ----------------------------- INCIDENCE DATA --------------------------------

## IF MORE THAN 5 YEARS SPECIFIED, KEEP LAST 5
    dataInc <- dataInc[which(dataInc$YEAR>=(max(dataInc$YEAR)-4) 
                             & dataInc$YEAR<=max(dataInc$YEAR)),]

## DUPLICATING DATA FOR ALL SITES (BUT NMSC, C44)
    dataAll <- dataInc[which(dataInc$ICD10GROUP!="C44"),] 
    dataAll$ICD10GROUP <- "ALLbC44"
    dataAll$ICD10GROUPLABEL <- "11 All sites but C44"
    dataAll <- aggregate(dataAll$CASES, by=list(dataAll$YEAR,
                                                dataAll$ICD10GROUP,
                                                dataAll$ICD10GROUPLABEL,
                                                dataAll$SEX,
                                                dataAll$AGE_GROUP, 
                                                dataAll$MORPHOLOGY,
                                                dataAll$BEHAVIOUR,
                                                dataAll$BASIS), FUN=sum)
    colnames(dataAll) <- c("YEAR", "ICD10GROUP","ICD10GROUPLABEL" ,
                           "SEX", "AGE_GROUP","MORPHOLOGY","BEHAVIOUR" ,
                           "BASIS","CASES")
    dataInc <- rbind(dataInc,dataAll)

## DUPLICATING DATA FOR BOTH SEXES
    dataAll <- dataInc			
    dataAll$SEX <- 3
    dataAll <- aggregate(dataAll$CASES, by=list(dataAll$YEAR,
                                                dataAll$ICD10GROUP,
                                                dataAll$ICD10GROUPLABEL,
                                                dataAll$SEX,
                                                dataAll$AGE_GROUP, 
                                                dataAll$MORPHOLOGY,
                                                dataAll$BEHAVIOUR,
                                                dataAll$BASIS), FUN=sum)
    colnames(dataAll) <- c("YEAR", "ICD10GROUP","ICD10GROUPLABEL" ,"SEX", 
                           "AGE_GROUP","MORPHOLOGY","BEHAVIOUR" ,"BASIS",
                           "CASES")
    dataInc <- rbind(dataInc,dataAll)


## REMOVING CASES OF UNKNOWN AGE (Necessary?)
    dataInc <- dataInc[which(dataInc$AGE_GROUP!=19),]		


## ----------------------------- POPULATION DATA -------------------------------

## RESTRICTING VARIABLES
    dataPop <- dataPop[,c("YEAR","AGE_GROUP_LABEL","AGE_GROUP","SEX","COUNT")]
	
## ADDING POPULATION FOR BOTH SEXES
    dataAll <- dataPop    		
    dataAll$SEX <- 3
    dataAll <- aggregate(dataAll$COUNT, by=list(dataAll$YEAR,
                                                dataAll$SEX,
                                                dataAll$AGE_GROUP,
                                                dataAll$AGE_GROUP_LABEL), 
                         FUN=sum)
	colnames(dataAll) <- c("YEAR", "SEX", "AGE_GROUP","AGE_GROUP_LABEL","COUNT")
	dataPop <- rbind(dataPop,dataAll)		



################################## ANALYSIS #################################### 

## CALCULATING ASR
    data <- CalcASR(dataInc, dataPop, standpop, strat=c("YEAR", "SEX"))



############################ PREPARATION OF OUTPUT ############################# 

## STARTING HTML REPORT
    filename <- paste(out,".", fileType, sep = "" ) 

    report <- paste("<HTML><HEAD><link rel=\"stylesheet\" href=\"", 
                    paste(sep="/", script.basename, 
                          "CI5EditorialTables.css"),"\">", sep="")
    report <- paste(report,"<link rel=\"stylesheet\" href=\"",
                    paste(sep="/", script.basename, 
                          "CI5EditorialTablesPrint.css"),
                          "\" media=\"print\"></HEAD><BODY>", sep="")


## CREATING TWO PAGES (ONE FOR THE NUMBER OF CASES, ONE FOR THE ASR)
    params <- c("N","asr")

    for(param in params){

        # HEADER
            title <- addHTMLTitle(header,attributes=c(),size=2,align="center")    	
            report <- paste(report,title,sep="")

		# TITLE
		    if(param=="N"){title <- "CI5 Editorial Table 1<br/>Number of cases 
                            in major diagnosis groups in single calendar years 
                            of observation"}
		    if(param=="asr"){title <- "CI5 Editorial Table 2<br/>ASR in major 
                            diagnosis groups in single calendar years 
                            of observation"}
	    	title <- addHTMLTitle(title,attributes=c(),size=4,align="center")
		    report <- paste(report,title,sep="")
        
        ## ALIGNMENT = CENTER
            report <- paste("<div align=\"center\">",report,sep="")		
		
        ## PROCESSING EACH SEX
		    for(sex in unique(data$SEX)){	
		
                # SELECTING DATA
		            tempData <- data[data$SEX==sex,]    								
		    
                # ADDING SITE LABELS
                    labs <- GetSiteLabels(dataInc,sex)
		            mydata <- merge(tempData,labs,by=c("ICD10GROUP"), sort=F)
        
                # PROCESSING DATA
                    dataOut <- mydata[,c("ICD10GROUPLABEL",
                                         "ICD10GROUP","YEAR",
                                         param)]
                    dataOut[,4]<- as.numeric(dataOut[,4])
                    dataOut$SITE <- paste(dataOut$ICD10GROUPLABEL,
                                          " (",dataOut$ICD10GROUP,")",sep="")
        
                    dataOut <- dataOut[,c("SITE","YEAR",param)]
                    dataOut <- dcast(dataOut, 
                                     factor(SITE,levels=unique(SITE)) ~ YEAR, 
                                     fun.aggregate = sum, value.var=param, 
                                     na.rm = TRUE)
                    colnames(dataOut)[[1]] <- "SITE"
        
                # DATA TO BE DISPLAYED (with percentages)			
			        dispOut <- dataOut
				    for(i in 2:ncol(dispOut)){
    					dispOut[,i] <- paste(as.character(dataOut[,i])," (",round(dataOut[,i] * 100 / sum(dataOut[which(dataOut$SITE=="All sites but C44 (ALLbC44)"),i]),1),")",sep="")
	    			}
			
				# SAVING DATA FRAMES
		    		assign(paste("dataOut",sex,sep=""), dataOut) 
				    assign(paste("dispOut",sex,sep=""), dispOut)
			
				# TABLE 
				    if(sex==1){text <- "MALES"}
				    if(sex==2){text <- "FEMALES"}
				    if(sex==3){text <- "BOTH SEXES"}
				    title <- addHTMLTitle(text,attributes=c(),
                                          size=4,align="center")
				    report <- paste(report,title,sep="")
				    content <- DFToHTML(dispOut)			
				    report <- paste(report,content,sep="")
            }
		
			# BAR PLOT FOR ALL SITES BUT C44 (BOTH SEXES)
			    title <- addHTMLTitle("ALL SITES BUT SKIN<br/>(BOTH SEXES)",
                                    attributes=c(),size=4,align="center")	
			    report <- paste(report,title,sep="")
			    my.file <- tempfile()
			    svg(my.file, width = 5, height = 3)
			    par(mar=c(2,2,1,0))
			
			    maximum <- max(as.numeric(dataOut3[which(dataOut3$SITE=="All sites but C44 (ALLbC44)"),2:ncol(dataOut3)]))
			    values <- as.numeric(dataOut3[which(dataOut3$SITE=="All sites but C44 (ALLbC44)"),2:ncol(dataOut3)]) * 100 / maximum
			    myplot <- barplot(values, names.arg=colnames(dataOut3)[2:ncol(dataOut3)],cex.names=0.8,cex.axis=0.8)
			    #text(myplot, values-15, paste(round(values, 1),"%",sep=""),cex=0.8,pos=3) 
			    print(myplot)
			    dev.off()
			
                MyImage <- readChar(my.file, file.info(my.file)$size)
			    report <- paste(report,MyImage,sep="")
			    report <- paste(report,"</div><br/>",sep="")
		}	
	
	# SENDING REPORT TO CONSOLE
    	report <- paste(report,"</BODY></HTML>",sep="")
    	write(report, file = filename)
    	cat(paste("-outFile",filename,sep=":"))
	
	
	