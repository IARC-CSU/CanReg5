# Dependencies for graphics
		
	# Check that ggplot is installed
	source(paste(sep="/", script.basename, "Funct_misc.R"))  # Misc functions
	if(!is.installed("ggplot2")){
		load.fun("ggplot2")
	}       
	require(ggplot2) 
	
	
############################## NEW FUNCTIONS ############################## 	
    
StartGraph <- function(filename, filetype, height=5, width=7 ){
    
    if(filetype=="png"){png(filename)}
    if(filetype=="svg"){svg(filename)}
    if(filetype=="pdf"){pdf(filename,height=height ,width=width)}
    
} 
    
  
    
    
############################## OLD FUNCTIONS ############################## 
    
    
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
	plotAgeSpecRates <- function(data, logr, smooth, header, label, number, agegrs, color){
	
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
			if(color==1){g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL, colour=ICD10GROUPLABEL))}
			if(color==0){g1 <- ggplot(height=600, width=800, data=data, aes(x = LABEL, y = RATE, group=ICD10GROUPLABEL))}
			
		g1 <- g1 + scale_x_discrete(limits=agegrs$AGE_GROUP_LABEL)
		
		# Axis labels & ticks
		g1 <- g1 + xlab("\nAge group") + ylab("Age-specific rates") +labs(colour="")
		if(color==0){g1 <- g1+ theme_bw()}
		g1 <- g1 + theme(axis.text.x = element_text(angle=90, size=8, hjust=0.5, vjust=0.5))
		
		# Legend
		g1 <- g1 + theme(legend.position='bottom', legend.key = element_blank())
		if(color==1){g1 <- g1 + guides(col = guide_legend(nrow = ceiling(number/3)))}
		if(color==0){g1 <- g1 + guides(linetype = guide_legend(nrow = ceiling(number/3), title=NULL))}
		
		# Titles
		label <- paste(label," (",sex,")", sep="")
		g1 <- addGGtitle(g1, header, label)
        
		# Variable parameters
		
		#write.csv(data,"c:/Users/antonis/Desktop/debug.csv",row.names=F)
		
        
		# Smoothing
		if(smooth!=FALSE){
		    g1 <- g1 + stat_smooth(se = FALSE, n=smooth, na.rm=TRUE) 
		}else{
		    if(color==0){g1 <- g1 + geom_line(aes(linetype=ICD10GROUPLABEL)) + geom_point()}
		    if(color==1){g1 <- g1 + geom_line() + geom_point()}
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
			data$ASR <- as.numeric(data$asr)
	
		# Create graph
			g1 <- ggplot(height=600, width=800, data=data, aes(x = YEAR, y = ASR, group=SITE ,colour=SITE))
		    g1 <- g1 + theme_bw()
        
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
			g1 <- g1 + theme(axis.text.x = element_text(size=12, hjust=0.5, vjust=0.5, angle=90))
			g1 <- g1 + theme(panel.grid.minor = element_blank(),panel.grid.major.x = element_blank())
		
		# Legend		
			g1 <- g1 + theme(legend.position='bottom')
			g1 <- g1 + guides(col = guide_legend(ncol = 3))
		    g1 <- g1 + theme(legend.key = element_blank())
        
		# Titles
			label <- paste(label," (",sex,")", sep="")
			g1 <- addGGtitle(g1, header, label)

	return(g1)
}



# Draws ASR Pyramid
	plotASRPyramid <- function(data, header, label, number=1000){
		
		# Margins & Layout
		layout(matrix(1:2,nrow=1))
		par(oma=c(1,1,4,1))	
		par(pin=c(6	,6))
				
		# Data 
		data <- head(data,number)
		males <- data$ASR_M
		females <- data$ASR_F
			
		# Maximum value and ticks for the axes
		maxVal <- max(max(males),max(females))
		axisVals <- seq(0,maxVal+20, by=20)

		
		# Males plot ----------------------------------
		xlimM <- c(-max(axisVals),0)
		par(mai=c(1,1,0.5,.5))	
		barplot(-rev(males), horiz=T, main="Males", space=0, col="#006699", xlim=xlimM, axes=F, axisnames=F, cex.axis =0.7, cex.main= 0.7, xaxt="n", yaxt="n")
		axis(1,at=-axisVals, labels=sprintf("%1.0f",axisVals),cex.axis =0.5)
		mtext("ASR per 100,000", side = 1, line = 2, outer=F, cex =0.6)	
		
		# Females plot ----------------------------------
		xlimF <- c(0,max(axisVals))
		par(mai=c(1,.5,0.5,1))
		barplot(rev(females), horiz=T, main="Females", space=0, col="#CC6666", xlim=xlimF, axes=F,axisnames=F, cex.axis =0.7, cex.main= 0.7)
		axis(1,at=axisVals, labels=sprintf("%1.0f",axisVals),cex.axis =0.5)
				
		# Vertical labelling (names of sites)
		sites <- rev(as.character(data$ICD10GROUPLABEL))
		#axis(2, at=c(1:length(sites))-0.5, labels=sites, tcl=0, lty=0, las=1, cex.axis =0.5, pos=0, hadj=0.5)
		mtext(side = 2, text = sites, line = 2.5, las=1,cex =0.5, at=c(1:length(sites))-0.5,adj = 0.5)		
				
		# Graph titles
		mtext(header, side = 3, line = 2, outer=T)
		mtext(label, side = 3, line = 1, outer=T, cex =0.8)
		#mtext("Most Common Cancers", side = 3, line = 1, outer=T, cex =0.8)
		mtext("ASR per 100,000", side = 1, line = 2, outer=F, cex =0.6)	
}





# Draws Population Pyramid
	plotPopulationPyramid <- function(data, header, label, showvals=1, color=0){
		    
		# Margins & Layout
		par(oma=c(1,1,3,1), mai=c(1,1,1,.2))  # oma is outed margin, mai is inner margin
		layout(matrix(1:2,nrow=1))  # 1 row, but two graphs

		# Data (counts)
		males <- data$COUNT[data$SEX==1]
		males2 <- -males
		females <- data$COUNT[data$SEX==2]
		
		# Percentage data for the axis
		malesPerc <- males * 100 / sum(males)
		femalesPerc <- females * 100 / sum(females)
    
    # Max percent value and values for axis
		maxPerc <- max(max(malesPerc),max(femalesPerc))
		axisVals <- seq(0,maxPerc+6, by=5)
		
		if(color==1){col <- c("#006699","#CC6666")}else{col <- c("grey","grey")}
    
		# Males plot --------------------------------
		xlimM <- c(-max(axisVals),0)
		barplot(-malesPerc,
          horiz=T,main="Males",
          space=0,
          col=col[1],
          xlim=xlimM,
          axes=F,
          axisnames=F,
          cex.axis =0.7,
          cex.main =0.8,
          xaxt="n", yaxt="n", 
          line=1)
		
    # Percentages labels
		axis(1,at=-axisVals, labels=sprintf("%1.1f",axisVals),cex.axis =0.7)
		
    # Number of cases for males (on the side of the graph)
    if(showvals==1){text(x = -max(axisVals), y = as.numeric(unique(data$AGE_GROUP))+0.5,labels=males, cex=0.6, adj=0)}
    		
    # Subtitle (down) of the graph
    mtext(text = "% of the male population", side = 1, line = 3, outer = F, cex = 0.7, font = 1)
		
    
		# Females plot --------------------------------
		xlimF <- c(0,max(axisVals))
		par(mai=c(1,.2,1,1))
		barplot(femalesPerc,
          horiz=T,
          main="Females",
          space=0,col=col[2],
          xlim=xlimF,
          axes=F,axisnames=F,
          cex.axis =0.7,
          cex.main =0.8, 
          line=1)
		
    # Percentages labels
		axis(1,at=axisVals, labels=sprintf("%1.1f",axisVals),cex.axis =0.7)
		
		# Number of cases for females (on the side of the graph)
		if(showvals==1){text(x = max(axisVals), y = as.numeric(unique(data$AGE_GROUP))+0.5,labels=females, cex=0.6, adj=1)}
		
    # Subtitle (down) of the graph
		mtext(text = "% of the female population", side = 1, line = 3, outer = F, cex = 0.7, font = 1)
    
    
    # Overall plot stuff  --------------------------------
    
    # Labels for age groups
		mylabels <- as.character(unique(data$AGE_GROUP_LABEL))
		axis(2, at=as.numeric(unique(data$AGE_GROUP)+0.5), labels=mylabels, tcl=0, lty=0, las=1, cex.axis =0.6, pos=0, hadj=0.5)   
	
		# Main title of the graph
		mtext(text = header, side = 3, line = 0, outer = TRUE, cex = 1.2, font = 2)
}


    
    
			
# PLOTS AGE SPECIFIC TRENDS
	plotAgeSpecificTrends <- function(data, logr, smooth, header, label){
	    
	    # Sex being plotted
	    sex <- data$SEX[1]
	    if(sex==1){sex <- "Males"}else{sex <- "Females"}
	    
        # Site being plotted
	    site <- data$SITE[1]
        
	    # Sorting data
	    data <- data[order(data$SEX, data$YEAR, data$AGE_GROUP), ]
	    
	    # Converting data
	    data$YEAR <- as.factor(data$YEAR)
	    data$AGE_GROUP <- as.factor(data$AGE_GROUP)
	    data$RATE <- as.numeric(data$RATE)
	    
	    # Create graph
	    g1 <- ggplot(height=600, width=800, data=data, aes(x = YEAR, y = RATE, group=AGE_GROUP ,colour=AGE_GROUP))
	    g1 <- g1 + theme_bw()
	    
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
	    g1 <- g1 + xlab("\n") + ylab("Age-Specific Rates per 100,000") +labs(colour="")
	    g1 <- g1 + theme(axis.text.x = element_text(size=12, hjust=0.5, vjust=0.5, angle=90))
	    g1 <- g1 + theme(panel.grid.minor = element_blank(),panel.grid.major.x = element_blank())
        
	    # Legend		
	    g1 <- g1 + theme(legend.position='bottom')
	    g1 <- g1 + guides(col = guide_legend(ncol = 3))
	    g1 <- g1 + theme(legend.key = element_blank())
	    # Titles
	    label <- paste(label," (",sex,"), ",site, sep="")
	    g1 <- addGGtitle(g1, header, label)
	    
	    return(g1)
	}    