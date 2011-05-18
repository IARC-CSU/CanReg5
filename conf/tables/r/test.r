# 1st argument is either pdf, png, ps or svg
# 2nd argument is report file name - or output file name if you want
# 3rd argument is population file name
# 4th argument is incidence file name

Args <- commandArgs(TRUE)
# garbage <- dev.off()

if (Args[1] == "png") {
    filename <- paste( Args[2], ".png", sep = "" )
    png(file=filename, bg="transparent")
} else if (Args[1] == "pdf") { 
    filename <- paste( Args[2], ".pdf" , sep = "")
    pdf(file=filename) 
} else { 
    filename <- paste( Args[2], ".png" , sep = "")
    png(file=filename, bg="transparent") 
}

# call proper function
# in this test we call the rainbow wheel thingy stolen from the demo(graphics)
par(bg = "gray")
pie(rep(1,24), col = rainbow(24), radius = 0.9)
dev.off()

# write the name of any file created by R to out
cat(filename)

# cat("\n")
# cat(Args[3])
# cat("\n")
# cat(Args[4])