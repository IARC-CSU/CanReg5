# Dependencies for graphics
		
	# Check that ggplot is installed
	source(paste(sep="/", script.basename, "Funct_misc.R"))  # Misc functions
	if(!is.installed("ggplot2")){
		load.fun("ggplot2")
	}       
	require(ggplot2) 
	
		
# Add title and subtitle to a ggplot
	addGGtitle <- function(pl, main="Main Title", sub="Missing"){

		if(sub=="Missing"){
			pl <- pl + ggtitle(eval(parse(text=paste("expression(atop(\"",main,"\",","))", sep=""))))
		}else{
			pl <- pl + ggtitle(eval(parse(text=paste("expression(atop(\"",main,   "\",",  " atop(\"",  sub , "\",\"\")))",  sep=""))))				
		}
		
		return(pl)
	}

	
# Draws Age Spec Incidence Rates for the top X cancers
	plotAgeSpecRates <- function(data, logr, smooth, header, label, number, agegrs){
	
		# Getting highest age group in dataset
			maxGr <- max(data$AGE_GROUP)

		# Sex being plotted
			sex <- data$SEX[1]
			if(sex==1){sex <- "Males"}else{sex <- "Females"}
	
		# Merging age group labels with data
			data <- merge(data,agegrs,by=c("AGE_GROUP"),all.y=TRUE)
			colnames(data) <- c("AGE_GROUP","SEX","ICD10GROUP","ICD10GROUPLABEL","CASES","COUNT","RATE","LABEL")

		# Sorting data
			data <- data[order(data$SEX, data$ICD10GROUP, data$AGE_GROUP), ]
	
		# Create graph
			
			# Graph itself
			#g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL, colour=ICD10GROUPLABEL), plot.title=header)
			g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL, colour=ICD10GROUPLABEL))
			g1 <- g1 + scale_x_discrete(limits=agegrs$AGE_GROUP_LABEL)
						
			# Axis labels & ticks
			g1 <- g1 + xlab("\nAge group") + ylab("Age-specific rates") +labs(colour="")
			g1 <- g1 + theme(axis.text.x = element_text(angle=90, size=8, hjust=0.5, vjust=0.5))
			
			# Legend
			g1 <- g1 + theme(legend.position='bottom')
			g1 <- g1 + guides(col = guide_legend(nrow = ceiling(number/3)))
			
			# Titles
			# OK (main title only): g1 <- g1 + ggtitle(eval(parse(text=paste("expression(atop(\"",header,"\",","))", sep=""))))
			# OK (title and subtitle): g1 <- g1 + ggtitle(eval(parse(text=paste("expression(atop(\"",header,   "\",",  " atop(\"",  label , "\",\"\")))",  sep=""))))					
			label <- paste(label," (",sex,")", sep="")
			g1 <- addGGtitle(g1, header, label)
			
			# Variable parameters
			
				# Smoothing
					if(smooth!=FALSE){
						g1 <- g1 + stat_smooth(se = FALSE, n=smooth, na.rm=TRUE) #+ geom_point()
					}else{
						g1 <- g1 + geom_line() + geom_point()
					}	

				# Scale (log or not log)
					if(logr==TRUE){
						g1 <- g1 + scale_y_log10(breaks=c(1,10,100,1000)) 
					}else{
						g1 <- g1 + scale_y_continuous(breaks=c(0,100,200,300,400,500,600,700,800,900,1000,1100,1200,1300,1400,1500,1600,1700,1800,1900,2000))
					}


	return(g1)
	
}







# Draws Time trends (ASR) for the top X cancers
	plotTimeTrends <- function(data, logr, smooth, header, label, number){
	
		# Sex being plotted
			sex <- data$SEX[1]
			if(sex==1){sex <- "Males"}else{sex <- "Females"}
				
		# Sorting data
			data <- data[order(data$SEX, data$YEAR, data$SITE), ]
	
		# Converting data
			data$YEAR <- as.factor(data$YEAR)
			data$ASR <- as.numeric(data$ASR)
	
		# Create graph
			g1 <- ggplot(height=600, width=800, data=data, aes(x = YEAR, y = ASR, group=SITE ,colour=SITE))
				
		# Lines & Smoothing
			if(smooth!=FALSE){
				g1 <- g1 + stat_smooth(se = FALSE, n=smooth, na.rm=TRUE) #+ geom_point()

			}else{
				g1 <- g1 + geom_line() + geom_point()
			}	
			
		# Scale (log or not log)
			if(logr==TRUE){
				g1 <- g1 + scale_y_log10(breaks=c(1,5,10,20,50,100,200,500,1000)) 
			}else{
				g1 <- g1 + scale_y_continuous(breaks=c(0,10,20,30,40,50,60,70,80,90,100,110,120,130,140,150,200))
			}
		# Axis labels & ticks
			g1 <- g1 + xlab("\n") + ylab("Age-Standardized Rates (ASR) per 100,000") +labs(colour="")
			g1 <- g1 + theme(axis.text.x = element_text(size=12, hjust=0.5, vjust=0.5))
		
		# Legend		
			g1 <- g1 + theme(legend.position='bottom')
			g1 <- g1 + guides(col = guide_legend(ncol = 3))

		# Titles
			label <- paste(label," (",sex,")", sep="")
			g1 <- addGGtitle(g1, header, label)

	return(g1)
	
}


