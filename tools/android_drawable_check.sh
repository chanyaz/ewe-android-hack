#!/bin/bash

#THIS SCRIPT HELPS TO VERIFY OUR DRAWABLES ARE GETTING MOVED PROPERLY - IT PERFORMS THE FOLLOWING STEPS
# 1) IF AN IMAGE IN YOUR PROJECTS DRAWABLE DIRECTORY IS ALSO IN THE DESIGN FOLDER WE COMPARE HASHES AND MAKE SURE THEY ARE THE SAME
# 2) IF AN IMAGE IN YOUR PROJECTS DRAWABLE DIRECTORY IS AVAILABLE AT OTHER DENSITIES IN DESIGN, THEN WE PRINT THE FILE NAME


#this is where the designers put their work...
DESIGN_RES="/Users/jdrotos/Dropbox/Design/Expedia Flights/4. Assets/Android"

#this is the project res directory
PROJECT_RES="/Users/jdrotos/Documents/projects/ExpediaFlights/project/res"

#this is the density we expect to have valid project assets 
PRIMARY_DENSITY="xhdpi"


while getopts "d:r:p:" option
do
        case "${option}"
        in
                d) DESIGN_RES=${OPTARG};;
                r) PROJECT_RES=${OPTARG};;
                p) PRIMARY_DENSITY="${OPTARG}";;
        esac
done


if [ ! -d $DESIGN_RES ]  || [ ! -d $PROJECT_RES ] ; then

read -d '' APP_DESC <<"EOF"
	THIS SCRIPT HELPS TO VERIFY OUR DRAWABLES ARE GETTING MOVED PROPERLY - IT PERFORMS THE FOLLOWING STEPS
 	1) IF AN IMAGE IN YOUR PROJECTS DRAWABLE DIRECTORY IS ALSO IN THE DESIGN FOLDER WE COMPARE HASHES AND MAKE SURE THEY ARE THE SAME
 	2) IF AN IMAGE IN YOUR PROJECTS DRAWABLE DIRECTORY IS AVAILABLE AT OTHER DENSITIES IN DESIGN, THEN WE PRINT THE FILE NAME
 	3) IF AN IMAGE IN YOUR PROJECTS DRAWABLE DIRECTORY IS SUBSTANTIALLY OLDER THAN THAT OF THE PRIMARY DENSITY, WE LET YOU KNOW
EOF
	echo "${APP_DESC}"
	echo "usage: $0 -d <design res directory> -r <project res directory> -p <primary density>"
	echo "example: $0 -d \"/Users/jdrotos/Dropbox/Design/Expedia Flights/4. Assets/Android\" -r \"/Users/jdrotos/Documents/projects/ExpediaFlights/project/res\" -p \"xhdpi\""
	exit 1
fi


#secondary density
DENSITIES=( [0]="ldpi" [1]="mdpi" [2]="hdpi" [3]="xhdpi" )

#second difference for notice
ONE_DAY_IN_SECONDS=86400
SECONDS_DIFF=$ONE_DAY_IN_SECONDS

#counters
badhashcount=0
densityuncopiedtotal=0
creationtimethresholdfiles=0
primarydensityfilecount=0
densitymissingtotal=0
crashyninepatches=0
crashyhdpiimages=0

echo -e "\n*** SEARCHING FOR CRASHY 1px HDPI IMAGES***"
for file in $PROJECT_RES/drawable-hdpi/*[^9].png
do
    curImageHeight=$(identify -format "%h" "${file}")
    curImageWidth=$(identify -format "%w" "${file}")
    if [ "${curImageHeight}" == "1" -o "${curImageWidth}" == "1" ]; then
        echo -e "IMAGE ${curImageHeight}x${curImageWidth} - ${file}"
        crashyhdpiimages=$[crashyhdpiimages + 1]
    fi
done
echo -e "${crashyhdpiimages} crashy 1px hdpi images"

echo -e "\n*** SEARCHING FOR CRASHY NINE-PATCH IMAGES***"
for file in $PROJECT_RES/drawable-*/*.9.png
do
    curImageHeight=$(identify -format "%h" "${file}")
    curImageWidth=$(identify -format "%w" "${file}")
    if [ "${curImageHeight}" == "3" -o "${curImageWidth}" == "3" ]; then
        echo -e "NINEPATCH ${curImageHeight}x${curImageWidth} - ${file}"
        crashyninepatches=$[crashyninepatches + 1]
    fi
done
echo -e "${crashyninepatches} crashy 3px 9patch images"


echo -e "\n*** CHECKING IF DESIGN FILES MATCH FILES IN RES ***"
for density in ${DENSITIES[*]}
do
	mismatches=0
	echo -e "\tCHECKING DENSITY:${density}"
	for file in $PROJECT_RES/drawable-${density}/*
	do	
		filename=$(basename "${file}")
		filename_design=$DESIGN_RES/drawable-${density}/${filename}
		
		if [ -f "${filename_design}" ];
		then
			if ! diff -q "${filename_design}" "${file}" > /dev/null;
			then
				echo -e "\t\tDESIGN AND RES FILES DIFFER : ${filename}"
				if [ "${filename_design}" -nt "${file}" ]; then
					echo -e "\t\t\tDesign is newer than res"
				else
					echo -e "\t\t\tRes is newer than design"
				fi

				badhashcount=$[badhashcount + 1]
				mismatches=$[mismatches +1]
			fi
		fi
	done
	echo -e "\t${mismatches} mismatches!"
	echo -e "\n"
done

echo -e "\n*** CHECKING FOR AVAILABLE ( BUT UNCOPIED ) ASSETS ***"
for file in $PROJECT_RES/drawable-$PRIMARY_DENSITY/*
do
	filename=$(basename "${file}")
	filename_design=$DESIGN_RES/drawable-${density}/${filename}
	
	if [ -f "${filename_design}" ];
	then	
		for density in ${DENSITIES[*]}
		do
			
			if [ "${density}" == "${PRIMARY_DENSITY}" ]; then
				continue
			fi
	
			fname_des=$DESIGN_RES/drawable-${density}/${filename}
			fname_res=$PROJECT_RES/drawable-${density}/${filename}

			if [ -f "${fname_des}" ];
			then
				if [ ! -f "${fname_res}" ];
				then
					echo -e "\tWe have an ${density} asset for ${filename} in design but not in res"
					#echo "You should run cp ${fname_des} ${fname_res}"
					densityuncopiedtotal=$[densityuncopiedtotal + 1]
				fi	
			fi

		done
	fi
done

echo -e "\n*** CHECKING TIME DIFFERENCES BETWEEN DENSITIES ***"
for file in $PROJECT_RES/drawable-$PRIMARY_DENSITY/*
do
	filename=$(basename "${file}")
	filetime=$(stat -f "%m" "${file}")

	for density in ${DENSITIES[*]}
	do
		if [ "${density}" == "${PRIMARY_DENSITY}" ]; then
			continue
		fi
		
		filename_other=$PROJECT_RES/drawable-${density}/${filename}
		
		if [ -f "${filename_other}" ]; then
			
			filetime_other=$(stat -f "%m" "${filename_other}")

			filetime_diff=$(expr $filetime - $filetime_other)
			abs_diff=${filetime_diff#-}	
			abs_diff_days=$(expr $abs_diff / $ONE_DAY_IN_SECONDS )
				
			if [ "${abs_diff}" -gt "${SECONDS_DIFF}" ]; then
				echo -e "\tThe ${density} version of ${filename} has a creation time that is ${filetime_diff} seconds ( ${abs_diff_days} days ) different from the ${PRIMARY_DENSITY} version" 
				creationtimethresholdfiles=$[creationtimethresholdfiles + 1]
			fi	

		fi
			
	done

done

echo -e "\n*** GENERATING DENSITY MAP ***"
densityTable="FILENAME\t"
for density in ${DENSITIES[*]}
do
    densityTable="${densityTable}|"
    if [ "${density}" == "${PRIMARY_DENSITY}" ]; then
        densityTable="${densityTable}${density}*\t"
    else
        densityTable="${densityTable}${density}\t"
    fi
done
for file in $PROJECT_RES/drawable-$PRIMARY_DENSITY/*
do
    echo -e ".\c"
    primarydensityfilecount=$[primarydensityfilecount + 1]
    filename=$(basename "${file}")
    densityTable="${densityTable}\n${filename}\t"
    for density in ${DENSITIES[*]}
    do
        densityTable="${densityTable}|"
        filename_other=$PROJECT_RES/drawable-${density}/${filename}

        if [ -f "${filename_other}" ]; then
            densityTable="${densityTable}x\t"
        else
            densityTable="${densityTable} \t"
            densitymissingtotal=$[densitymissingtotal + 1]
        fi
    done
    densityTable="${densityTable}\n"
done
echo -e "Done\n"
echo -e "${densityTable}" | column -t


echo -e "\n*** GENERATING DIMENSION MAP ***"
dimensionTable="FILENAME\t"
for density in ${DENSITIES[*]}
do
    dimensionTable="${dimensionTable}|"
    if [ "${density}" == "${PRIMARY_DENSITY}" ]; then
        dimensionTable="${dimensionTable}${density}*\t"
    else
        dimensionTable="${dimensionTable}${density}\t"
    fi
done
for file in $PROJECT_RES/drawable-$PRIMARY_DENSITY/*
do
    echo -e ".\c"
    filename=$(basename "${file}")
    dimensionTable="${dimensionTable}\n${filename}\t"
    for density in ${DENSITIES[*]}
    do
        dimensionTable="${dimensionTable}|"
        filename_other=$PROJECT_RES/drawable-${density}/${filename}

        if [ -f "${filename_other}" ]; then
            curImageDimen=$(identify -format "%wx%h" "${filename_other}")
            dimensionTable="${dimensionTable}${curImageDimen}\t"
        else
            dimensionTable="${dimensionTable} \t"
        fi
    done
    dimensionTable="${dimensionTable}\n"
done
echo -e "Done\n"
echo -e "${dimensionTable}" | column -t



echo -e "\n\n*** REPORT: ***"
echo -e "\t${badhashcount} files differ between design and res"
echo -e "\t${densityuncopiedtotal} missing but available density files"
echo -e "\t${creationtimethresholdfiles} files had creation times that differed from the ${PRIMARY_DENSITY} versions by more than ${SECONDS_DIFF} seconds"
echo -e "\t${primarydensityfilecount} files in the ${PRIMARY_DENSITY} drawable folder"
echo -e "\t${densitymissingtotal} missing density specific files"
