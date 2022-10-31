// CP210xSetIDsDlg.h : header file
//

#if !defined(AFX_CP210xSETIDSDLG_H__F10CDA21_3FAF_4B7D_9C29_8855C44684B9__INCLUDED_)
#define AFX_CP210xSETIDSDLG_H__F10CDA21_3FAF_4B7D_9C29_8855C44684B9__INCLUDED_

#if _MSC_VER > 1000
#pragma once
#endif // _MSC_VER > 1000

#include <string>
using namespace std;

#include "dbt.h"
#include "initguid.h"

// Guid for driver 5.0
DEFINE_GUID(GUID_INTERFACE_CP210x_50, 
0xa2a39220, 0x39f4, 0x4b88, 0xae, 0xcb, 0x3d, 0x86, 0xa3, 0x5d, 0xc7, 0x48);

// Guid for driver 4.20 through 4.40
DEFINE_GUID(GUID_INTERFACE_CP210x_420_440, 
0x993f7832, 0x6e2d, 0x4a0f, 0xb2, 0x72, 0xe2, 0xc7, 0x8e, 0x74, 0xf9, 0x3e);

// Guid for pre-4.20 driver, uncommon
DEFINE_GUID(GUID_INTERFACE_CP210x_410, 
0xE4768D57, 0x352E, 0x4495, 0x88, 0x0C, 0x05, 0x6A, 0x7C, 0xB3, 0x46, 0x11);

// Guid for CP210x device programmed as USBXpress devices (uses USBXpress driver)
DEFINE_GUID(GUID_INTERFACE_USBXPRESS, 
0x3c5e1462, 0x5695, 0x4e18, 0x87, 0x6b, 0xf3, 0xf3, 0xd0, 0x8a, 0xaf, 0x18);

/////////////////////////////////////////////////////////////////////////////
// CCP210xSetIDsDlg dialog

class CCP210xSetIDsDlg : public CDialog
{
// Construction
public:
	CCP210xSetIDsDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	//{{AFX_DATA(CCP210xSetIDsDlg)
	enum { IDD = IDD_CP210xSETIDS_DIALOG };
	CString	m_PID;
	CString	m_Power;
	CString	m_SerNum;
	CString	m_VID;
	CString	m_ProdString;
	CString	m_VersionMajor;
	CString	m_VersionMinor;
	CString	m_PartNum;
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CCP210xSetIDsDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	//{{AFX_MSG(CCP210xSetIDsDlg)
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	afx_msg void OnUpdateDeviceList();
	afx_msg void OnButtonProgramIds();
	afx_msg void OnSelchangeDeviceSelect();
	afx_msg BOOL OnDeviceChange(UINT nEventType, DWORD dwData);
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()

private:
	void FillDeviceList();
	void SetVid();
	void SetPid();
	void SetSelfPower();
	void SetMaxPower();
	void SetDeviceVersion();
	void SetSerialNumber();
	void SetProductString();

	void RegisterNotification();
    void UnregisterNotification();

    HDEVNOTIFY m_hNotifyDevNode;

	HANDLE	m_hUSBDevice;
	int		m_DeviceCurSel;
};

//{{AFX_INSERT_LOCATION}}
// Microsoft Visual C++ will insert additional declarations immediately before the previous line.

#endif // !defined(AFX_CP210xSETIDSDLG_H__F10CDA21_3FAF_4B7D_9C29_8855C44684B9__INCLUDED_)
