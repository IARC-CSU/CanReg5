makeFile <- function(out, fileType){

##Helper-function
is.installed <- function(mypkg) is.element(mypkg, installed.packages()[,1]) 

if (fileType == "png") {
		filename <- paste( out, "%03d.png", sep = "" )
		if(!is.installed("Cairo")) {
                    load.fun("Cairo")
		}
                # did we manage to install Cairo?
                if (is.installed("Cairo")){
                    require(Cairo)
                    CairoPNG(file=filename,
                        width = 1024, height = 1024, units = "px",
                        pointsize = 20, bg = "white") 
                } else {
                    png(file=filename,
                        width = 1024, height = 1024, units = "px",
                        pointsize = 20, bg = "white") 
                }
                # reset the filename just to return it properly
                filename <- paste( out, "001.png", sep = "" )
	} else if (fileType == "pdf") { 
		filename <- paste( out, ".pdf" , sep = "")
		pdf(file=filename) 	
	} else if (fileType == "ps") { 
		filename <- paste( out, ".ps" , sep = "")
		postscript(file=filename, onefile = TRUE) 
	} else if (fileType == "html") { 
		filename <- paste( out, ".html" , sep = "")
	} else if (fileType == "svg") { 
		filename <- paste( out, ".svg", sep = "" )
		# SVG needs the Cairo/RSvgDevice library installed
                # Using Cairo for now http://cran.r-project.org/web/packages/Cairo/Cairo.pdf
		if(!is.installed("Cairo")) {
                    load.fun("Cairo")
		}
		require(Cairo)
		CairoSVG(file=filename, pointsize = 12)
	} else if (fileType == "wmf") { 
		# This only works on windows
		filename <- paste( out, ".wmf" , sep = "")
		win.metafile(file=filename) 
	} else { 
		# defaults to pdf
		filename <- paste(out, ".pdf" , sep = "")
		pdf(file=filename) 
	}

return(filename)

}#End makeFile