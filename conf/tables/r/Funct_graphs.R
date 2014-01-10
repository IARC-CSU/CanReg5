# Dependencies for graphics
	#require(ggplot2)
	source(paste(sep="/", script.basename, "makeSureGgplot2IsInstalled.R")) # Checking that Ggplot is installed
	
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
	plotAgeSpecRates <- function(data, logr, smooth, header, label, number){
	
		# Getting highest age group in dataset
			maxGr <- max(data$AGE_GROUP)

		# Getting age group labels
			agegrs <- GetAgeGroupLabels(maxGr)
	
		# Sex being plotted
			sex <- data$SEX[1]
			if(sex==1){sex <- "M"}else{sex <- "F"}
	
		# Merging age group labels with data
			data <- merge(data,agegrs,by=c("AGE_GROUP"))
			colnames(data) <- c("AGE_GROUP","SEX","ICD10GROUP","ICD10GROUPLABEL","CASES","COUNT","RATE","LABEL")

		# Sorting data
			data <- data[order(data$SEX, data$ICD10GROUP, data$AGE_GROUP), ]
	
		# Create graph
			
			# Graph itself
			#g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL, colour=ICD10GROUPLABEL), plot.title=header)
			g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL, colour=ICD10GROUPLABEL))
			g1 <- g1 + scale_x_discrete(limits=agegrs$label)
						
			# Axis labels & ticks
			g1 <- g1 + xlab("\nAge group") + ylab("Age-specific rates") +labs(colour="")
			g1 <- g1 + theme(axis.text.x = element_text(angle=90, size=8, hjust=0.5, vjust=0.5))
			
			# Legend
			g1 <- g1 + theme(legend.position='bottom')
			g1 <- g1 + guides(col = guide_legend(nrow = ceiling(number/3)))
			
			# Titles
			# OK (main title only): g1 <- g1 + ggtitle(eval(parse(text=paste("expression(atop(\"",header,"\",","))", sep=""))))
			# OK (title and subtitle): g1 <- g1 + ggtitle(eval(parse(text=paste("expression(atop(\"",header,   "\",",  " atop(\"",  label , "\",\"\")))",  sep=""))))					
			g1 <- addGGtitle(g1, header, label)
			
			
			# Variable parameters
			
				# Smoothing
					if(smooth==TRUE){
						g1 <- g1 + stat_smooth(se = FALSE) + geom_point()
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