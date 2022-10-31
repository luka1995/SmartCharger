// CP210xSetIDs.h : main header file for the SETIDS application
//

#if !defined(AFX_CP210xSETIDS_H__96F8C52E_0D5E_471F_A144_2FC718A9CE14__INCLUDED_)
#define AFX_CP210xSETIDS_H__96F8C52E_0D5E_471F_A144_2FC718A9CE14__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#ifndef __AFXWIN_H__
	#error include 'stdafx.h' before including this file for PCH
#endif

#include "resource.h"		// main symbols

/////////////////////////////////////////////////////////////////////////////
// CCP210xSetIDsApp:
// See CP210xSetIDs.cpp for the implementation of this class
//

class CCP210xSetIDsApp : public CWinApp
{
public:
	CCP210xSetIDsApp();

// Overrides
	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CCP210xSetIDsApp)
	public:
	virtual BOOL InitInstance();
	//}}AFX_VIRTUAL

// Implementation

	//{{AFX_MSG(CCP210xSetIDsApp)
		// NOTE - the ClassWizard will add and remove member functions here.
		//    DO NOT EDIT what you see in these blocks of generated code !
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};


/////////////////////////////////////////////////////////////////////////////

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_CP210xSETIDS_H__96F8C52E_0D5E_471F_A144_2FC718A9CE14__INCLUDED_)
