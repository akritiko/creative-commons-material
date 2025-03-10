
#include <fstream.h>
#include <conio.h>
#include "./Member.h"
#include "./Copy.h"

/* H KLASH IOHandler APOTELEI OYSIASTIKA TON DIAXEIRISTH THS OLHS EFARMOGHS.
PERILAMVANEI THN EISAGOGH TON DEDOMENON APO TO XRHSTH, THN APOTHIKEYSH TOYS SE
KATALLHLOYS PINAKES GIA EYKOLOTERH DIAXEIRISH KAI TELOS THN EGGRAFH TON
APARAITHTON DEDOMENON SE ARXEIA. EPIPLEON ANALAMVANEI THN EPIKOINONIA ME TON
XRHSTH ME KATALLHLA DIAMORFOMENA MENOY*/

class IOHandler
{
private:
  Book * _Books; //PINAKAS ANTIKEIMENON TYPOY �ook
  Copy * _Copies; //PINAKAS ANTIKEIMENON TYPOY Copy
  Author * _Authors; //PINAKAS ANTIKEIMENON TYPOY Author
  Member * _Members; //PINAKAS ANTIKEIMENON TYPOY Member

  unsigned int _NOFBooks; //O ARITHMOS TON EGGEGRAMENON VIVLION ANA PASA STIGMH
  unsigned int _NOFCopies; //O ARITHMOS TON EGGEGRAMENON ANTITYPON ANA PASA STIGMH
  unsigned int _NOFAuthors; //O ARITHMOS TON EGGEGRAMENON SYGGRAFEON ANA PASA STIGMH
  unsigned int _NOFMembers; //O ARITHMOS TON EGGEGRAMENON MELON ANA PASA STIGMH

public:
  //O DHMIOYRGOS
  IOHandler();

  //OI PARAKATO METHODOI DIAVAZOYN APO TA ARXEIA KAI ENHMERONOYN TA PROSOPIKA MELH
  void ReadBooks(); //ENHMERONONTAI: _Books[] KAI _NOFBooks
  void ReadCopies(); //ENHMERONONTAI: _Copies[] KAI _NOFCopies
  void ReadAuthors(); //ENHMERONONTAI: _Authors[] KAI _NOFAuthors
  void ReadMembers(); //ENHMERONONTAI: _Members[] KAI _NOFMembers

  /* ANALAMVANOYN THN EISAGOGH STA ARXEIA (NA SHMEIOSOYME OTI H InsertBook()
  ANALAMVANEI KAI THN EISAGOGH TON SYGGRAFEON AFOY SYMFONA ME THN YLOPOIHSH
  MAS OI SYGGRAFEIS EISAGONTAI APO TON XRHSTH OS KOMMATI TOY
  BIBLIOY (TITLOY) */
  void InsertBook(); //VIVLIOY
  void InsertCopy(); //ANTITYPOY
  void InsertMember(); //MELOYS

  /* DIAGRAFH TOY EKASTOTE ANTIKEIMENOY APO TO ARXEIO POY TO PERIEXEI KAI
  ENHMEROSH TON ARXEION */
  void DeleteCopy( unsigned int TitleCode, unsigned int VictimsCode );
  void DeleteAllCopies( unsigned int TitleCode );

  /* ANALAMVANEI NA EREYNHSEI TOYS DANEISMOYS - KRATHSEIS ME VASH TO MELOS
  H TO ANTITYPO */
  void Search(); //STELNEI THN EPILOGH TOY XRHSTH SE ENA APO TA ALLA DYO
  void SearchCopy( unsigned int TitleCode, unsigned int CopyCode );
  void SearchMember( unsigned int MemberCode );

  /* ANALAMVANEI THN KRATHSH, DANEISMO H EPISTROGH VIVLIOY */
  void BorrowBook();
  void BookBook( unsigned int TitleCode, unsigned int MemberCode );
  void ReturnBook();

};
