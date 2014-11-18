# build distributions
# note that the configuration gets wrapped up in the build

.PHONY: public rpm deb

public:
	(cd public && component build -dc)

rpm: public
	./activator rpm:packageBin

deb: public
	./activator debian:packageBin
