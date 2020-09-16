 #!/bin/csh -f
  
  #***************************************************************************
  #
  # Makefile generator script
  #
  # (C) 2011 GeoSoft - Geotechnical Software Services
  # info@geosoft.no - http://geosoft.no
  #
  # This program is free software; you can redistribute it and/or
  # modify it under the terms of the GNU General Public License
  # as published by the Free Software Foundation; either version 2
  # of the License, or (at your option) any later version.
  #
  # This program is distributed in the hope that it will be useful,
  # but WITHOUT ANY WARRANTY; without even the implied warranty of
  # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  # GNU General Public License for more details.
  #
  # You should have received a copy of the GNU General Public License
  # along with this program; if not, write to the Free Software
  # Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
  #
  #***************************************************************************
  
  set file = Makefile

  rm -f $file
  touch $file

  printf "Source    = \\\n" >> $file

  foreach SourceFile ('ls *.c *.cc *.java *.f *.gif *.jpg')
		if ${SourceFile}
      printf "\t%s \\\n" ${SourceFile} >> $file
  end
  printf "\n" >> $file

  printf "RmiSource =\n\n" >> $file
  printf "Main      =\n\n" >> $file
  printf "include %s" '$(DEV_ROOT)/Makefile' >> $file