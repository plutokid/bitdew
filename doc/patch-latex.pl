#!/usr/bin/perl -w
#
# -i.orig -p
use strict ;
use Cwd;
use Cwd 'abs_path';
my $cwd = getcwd();
my $src = abs_path("$cwd/../src");


sub patch
{
  my $dossier=shift;
 
  my $prot_dossier=quotemeta($dossier);
 
  if ( -d $dossier ) {
    foreach (glob ($prot_dossier."/*")) {
      patch ($_) ;
    }
    print "Patch directory ($dossier)\n";
  } else {
      if ($dossier =~ /tex$/) {
      print "Patch file ($dossier)\n";
      } else {
	  print "Skip file ($dossier)\n";
      }
      open (OLD, "< $dossier") or die "cannot open $dossier : $!";
	  open (NEW, "> tmp.tex") or die "cannot open $dossier : $!";
      while (<OLD>) {
#	  s/<\/body>/$google_analytics/g;
	  s/::/\./g;
	  s/^Definition at line/%Definition at line/g;
	  s/^References/%References/g;
#fix for hypertatget which doesn't start at the beginning of line
	  s/\S\\hypertarget/\n\\hypertarget/g;
	  s/^Referenced by/%Referenced by/g;
	  s/$src\///g;

	  print NEW $_ or die "cannot write : $!";
      }
      close (NEW) or die "cannot close : $!";
      close (OLD)  or die "cannot close : $!";
      rename ("tmp.tex", $dossier) or die "cannot rename : $!";
  }
}
 

patch ("doxygen/latex");
