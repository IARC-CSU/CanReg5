

# Convert data frame to HTML table
	DFToHTML <- function(df){
		res <- "<table width=\"595\">"
	
		# Headings
			res <- paste(res,"<tr>",sep="")
			for(col in colnames(df)){
				if(col!=colnames(df)[1]){
					res <- paste(res,"<th align=\"right\"><b>",col,"</b></th>",sep="")
				}else{
					res <- paste(res,"<th><b>",col,"</b></th>",sep="")
				}
			}			
			res <- paste(res,"</tr>",sep="")
		
		# Data
		for(row in 1:nrow(df)){
			res <- paste(res,"<tr>",sep="")
			for(col in 1:ncol(df)){
				if(col!=1){
					res <- paste(res,"<td align=\"right\">",df[row,col],"</td>",sep="")
				}else{
					res <- paste(res,"<td>",df[row,col],"</td>",sep="")
				}
			}
			res <- paste(res,"</tr>",sep="")
		}
		res <- paste(res,"</table>",sep="")
	
		return(res)
	}
	
	
	
	
# Adding title
	addHTMLTitle <- function(title,attributes=c(),size=1,align="center"){
						
		# Getting attributes
			if('i' %in% attributes){title <- paste("<i>",title,"</i>",sep="")}
			if('b' %in% attributes){title <- paste("<b>",title,"</b>",sep="")}
			if('u' %in% attributes){title <- paste("<u>",title,"</u>",sep="")}

		# heading number
			title <- paste("<h",size,">",title,"</h",size,">",sep="")
					
		# Formatting size and alignment
			title <- paste("<div align=\"",align,"\">",title,"</div>",sep="")	
						
		return(title)	
	}
	
	
	