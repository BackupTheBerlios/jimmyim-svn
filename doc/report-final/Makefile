FILE=report

$(FILE).pdf: $(FILE).tex $(FILE).aux $(FILE).toc $(FILE).dvi
	pdflatex $(FILE).tex
	pdflatex $(FILE).tex

$(FILE).dvi: $(FILE).tex
	latex $(FILE).tex
	latex $(FILE).tex

$(FILE).aux: $(FILE).tex
	pdflatex $(FILE).tex

$(FILE).toc: $(FILE).aux
	pdflatex $(FILE).tex

clean:
	rm -f *.pdf *.toc *.log *.aux *.lof *.lot *.aux *.blg *.dvi *~