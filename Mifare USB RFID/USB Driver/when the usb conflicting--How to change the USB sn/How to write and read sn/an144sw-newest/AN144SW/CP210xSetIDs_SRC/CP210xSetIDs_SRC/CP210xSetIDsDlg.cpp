// CP210xSetIDsDlg.cpp : implementation file
//

#include "stdafx.h"
#include <string>
#include "CP210xManufacturingDll.h"
#include "CP210xSetIDs.h"
#include "CP210xSetIDsDlg.h"


#ifdef _DEBUG
#define new DEBUG_NEW
#undef THIS_FILE
static char THIS_FILE[] = __FILE__;
#endif

using namespace std;

/////////////////////////////////////////////////////////////////////////////
// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	//{{AFX_DATA(CAboutDlg)
	enum { IDD = IDD_ABOUTBOX };
	//}}AFX_DATA

	// ClassWizard generated virtual function overrides
	//{{AFX_VIRTUAL(CAboutDlg)
	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support
	//}}AFX_VIRTUAL

// Implementation
protected:
	//{{AFX_MSG(CAboutDlg)
	//}}AFX_MSG
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
	//{{AFX_DATA_INIT(CAboutDlg)
	//}}AFX_DATA_INIT
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CAboutDlg)
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
	//{{AFX_MSG_MAP(CAboutDlg)
		// No message handlers
	//}}AFX_MSG_MAP
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CCP210xSetIDsDlg dialog

CCP210xSetIDsDlg::CCP210xSetIDsDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CCP210xSetIDsDlg::IDD, pParent)
{
	//{{AFX_DATA_INIT(CCP210xSetIDsDlg)
	m_PID = _T("");
	m_Power = _T("");
	m_SerNum = _T("");
	m_VID = _T("");
	m_ProdString = _T("");
	m_VersionMajor = _T("");
	m_VersionMinor = _T("");
	m_PartNum = _T("");
	//}}AFX_DATA_INIT
	// Note that LoadIcon does not require a subsequent DestroyIcon in Win32
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void CCP210xSetIDsDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	//{{AFX_DATA_MAP(CCP210xSetIDsDlg)
	DDX_Text(pDX, IDC_EDIT_SET_PID, m_PID);
	DDV_MaxChars(pDX, m_PID, 4);
	DDX_Text(pDX, IDC_EDIT_SET_POWER, m_Power);
	DDV_MaxChars(pDX, m_Power, 2);
	DDX_Text(pDX, IDC_EDIT_SET_SER_NUM, m_SerNum);
	DDV_MaxChars(pDX, m_SerNum, 63);
	DDX_Text(pDX, IDC_EDIT_SET_VID, m_VID);
	DDV_MaxChars(pDX, m_VID, 4);
	DDX_Text(pDX, IDC_SET_PROD_STRING, m_ProdString);
	DDV_MaxChars(pDX, m_ProdString, 126);
	DDX_Text(pDX, IDC_EDIT_RELEASE_MAJOR, m_VersionMajor);
	DDV_MaxChars(pDX, m_VersionMajor, 2);
	DDX_Text(pDX, IDC_EDIT_RELEASE_MINOR, m_VersionMinor);
	DDV_MaxChars(pDX, m_VersionMinor, 2);
	DDX_Text(pDX, IDC_EDIT_PARTNUM, m_PartNum);
	//}}AFX_DATA_MAP
}

BEGIN_MESSAGE_MAP(CCP210xSetIDsDlg, CDialog)
	//{{AFX_MSG_MAP(CCP210xSetIDsDlg)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	ON_BN_CLICKED(IDC_UPDATE_DEVICE_LIST, OnUpdateDeviceList)
	ON_BN_CLICKED(IDC_BUTTON_PROGRAM_IDS, OnButtonProgramIds)
	ON_CBN_SELCHANGE(IDC_DEVICE_SELECT, OnSelchangeDeviceSelect)
	//}}AFX_MSG_MAP
	ON_WM_DEVICECHANGE()
END_MESSAGE_MAP()

/////////////////////////////////////////////////////////////////////////////
// CCP210xSetIDsDlg message handlers

BOOL CCP210xSetIDsDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.
	
	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon
	
	// Register for device notification
	RegisterNotification();

	// Init. member variables
	m_hUSBDevice = INVALID_HANDLE_VALUE;
	
	// Init. device selection to 0
	m_DeviceCurSel = 0;

	// Get devices, init. combo box with their names
	FillDeviceList();

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CCP210xSetIDsDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CCP210xSetIDsDlg::OnPaint() 
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, (WPARAM) dc.GetSafeHdc(), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CCP210xSetIDsDlg::OnQueryDragIcon()
{
	return (HCURSOR) m_hIcon;
}

void CCP210xSetIDsDlg::RegisterNotification()
{
	DEV_BROADCAST_DEVICEINTERFACE devIF = {0};
	devIF.dbcc_size = sizeof(devIF);    
	devIF.dbcc_devicetype = DBT_DEVTYP_DEVICEINTERFACE;    

	devIF.dbcc_classguid = GUID_INTERFACE_CP210x_50;
	m_hNotifyDevNode = RegisterDeviceNotification(GetSafeHwnd(), &devIF, DEVICE_NOTIFY_WINDOW_HANDLE);

	devIF.dbcc_classguid = GUID_INTERFACE_CP210x_420_440;
	m_hNotifyDevNode = RegisterDeviceNotification(GetSafeHwnd(), &devIF, DEVICE_NOTIFY_WINDOW_HANDLE);

	devIF.dbcc_classguid = GUID_INTERFACE_CP210x_410;
	m_hNotifyDevNode = RegisterDeviceNotification(GetSafeHwnd(), &devIF, DEVICE_NOTIFY_WINDOW_HANDLE);

	devIF.dbcc_classguid = GUID_INTERFACE_USBXPRESS;
	m_hNotifyDevNode = RegisterDeviceNotification(GetSafeHwnd(), &devIF, DEVICE_NOTIFY_WINDOW_HANDLE);
}

void CCP210xSetIDsDlg::UnregisterNotification()
{
	if (m_hNotifyDevNode)
	{
		UnregisterDeviceNotification(m_hNotifyDevNode);

		m_hNotifyDevNode = NULL;
	}
}

BOOL CCP210xSetIDsDlg::OnDeviceChange(UINT nEventType, DWORD dwData)
{
	switch(nEventType)
	{
	case DBT_DEVICEARRIVAL:        
			// A device has been inserted and is now available.
			
		// Fall through

	case DBT_DEVICEREMOVECOMPLETE:        
			// Device has been removed.
			
			OnUpdateDeviceList();

			break;    

	default:        
		break;    
	}

	return TRUE;
}


void CCP210xSetIDsDlg::OnUpdateDeviceList() 
{
	FillDeviceList();
}

void CCP210xSetIDsDlg::OnButtonProgramIds() 
{
	CComboBox*	pDevList	= (CComboBox*)GetDlgItem(IDC_DEVICE_SELECT);

	BeginWaitCursor();

	// Get the edit control values
	UpdateData(TRUE);

	if (pDevList)
	{
		// Open selected device.
		if (pDevList->GetCurSel() >= 0)
		{
			CP210x_STATUS status = CP210x_Open(pDevList->GetCurSel(), &m_hUSBDevice);

			if (status == CP210x_SUCCESS)
			{
				// If Vid checked, set value
				if (((CButton*)GetDlgItem(IDC_UPDATE_VID))->GetCheck())
				{
					SetVid();
				}
				// If Pid checked, set values
				if (((CButton*)GetDlgItem(IDC_UPDATE_PID))->GetCheck())
				{
					SetPid();
				}
				// If Power Attrib. checked, set values
				if (((CButton*)GetDlgItem(IDC_UPDATE_POWER_ATTRIB))->GetCheck())
				{
					SetSelfPower();
				}
				// If Max. Power checked, set values
				if (((CButton*)GetDlgItem(IDC_UPDATE_MAX_POWER))->GetCheck())
				{
					SetMaxPower();
				}
				// If Serial Number checked, set serial number.
				if (((CButton*)GetDlgItem(IDC_UPDATE_SER_NUM))->GetCheck())
				{
					SetSerialNumber();
				}
				// If Product String checked, set product string
				if (((CButton*)GetDlgItem(IDC_UPDATE_PROD_STRING))->GetCheck())
				{
					SetProductString();
				}
				// If Release Version checked, set value
				if (((CButton*)GetDlgItem(IDC_UPDATE_RELEASE_VERSION))->GetCheck())
				{
					SetDeviceVersion();
				}
				
				if (((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->GetCheck() && ((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->GetCheck())
				{
					if (MessageBox("WARNING:\n\nLocking the values is permenant.\nThis will perform an irreversible write to the part.\n\nAre you sure you want to LOCK all of these values?", "Warning", MB_YESNO) == IDYES)
					{
						CP210x_SetLockValue(m_hUSBDevice);
					}
				}

				// Reset the device.  The firmware will delay to give host application
				// enough time to close the handle.
				CP210x_Reset(m_hUSBDevice);

				// Close device.  We have about one second to close the device handle
				// before the device firmware resets.
				CP210x_Close(m_hUSBDevice);
				m_hUSBDevice = INVALID_HANDLE_VALUE;
			}
		}
	}

	DWORD start, end;
	
	start = end = GetTickCount();

	// Give firmware time to reset, and give the driver time to reload after firmware performs bus reset.
	while ((end - start) < 5000)
	{
		end = GetTickCount();
	}

	FillDeviceList();

	EndWaitCursor();	
}


void CCP210xSetIDsDlg::FillDeviceList()
{
	CP210x_DEVICE_STRING	devStr;
	DWORD					dwNumDevices	= 0;
	CComboBox*				pDevList		= (CComboBox*)GetDlgItem(IDC_DEVICE_SELECT);

	if (pDevList)
	{
		int numDevs = pDevList->GetCount();

		for (int i = 0; i < numDevs; i++)
		{
			pDevList->DeleteString(0);
		}

		CP210x_STATUS status = CP210x_GetNumDevices(&dwNumDevices);

		if (status == CP210x_SUCCESS)
		{
			for (DWORD d = 0; d < dwNumDevices; d++)
			{
				status = CP210x_GetProductString(d, devStr, CP210x_RETURN_FULL_PATH);

				if (status == CP210x_SUCCESS)
				{
					pDevList->AddString(devStr);
				}
			}
		}

		numDevs = pDevList->GetCount();

		if (numDevs > 0)
		{
			if (m_DeviceCurSel == CB_ERR)
				m_DeviceCurSel = 0;
		}

		pDevList->SetCurSel(m_DeviceCurSel);
	}

	OnSelchangeDeviceSelect();
}


void CCP210xSetIDsDlg::SetVid()
{
	// Convert string to integer
	WORD vid = (WORD)strtoul(m_VID, NULL, 16);

	// Call DLL function for setting Vid.
	CP210x_SetVid(m_hUSBDevice, vid);
}


void CCP210xSetIDsDlg::SetPid()
{
	// Convert string to integer
	WORD pid = (WORD)strtoul(m_PID, NULL, 16);

	// Call DLL function for setting Pid.
	CP210x_SetPid(m_hUSBDevice, pid);
}


void CCP210xSetIDsDlg::SetSelfPower()
{
	BOOL powerAttrib = FALSE;

	if (((CButton*)GetDlgItem(IDC_CHECK_SELF_POWERED))->GetCheck())
	{
		powerAttrib	= TRUE;
	}

	// Call DLL function for setting Power value.
	CP210x_SetSelfPower(m_hUSBDevice, powerAttrib);
}


void CCP210xSetIDsDlg::SetMaxPower()
{
	BYTE power;

	// Convert strings to integers
	power = (BYTE)strtoul(m_Power, NULL, 16);

	// Call DLL function for setting Power value.
	CP210x_SetMaxPower(m_hUSBDevice, power);
}


void CCP210xSetIDsDlg::SetDeviceVersion()
{
	BYTE verMajor, verMinor;
	WORD version;

	// Convert strings to integers
	verMajor = (BYTE)strtoul((CString)(m_VersionMajor.GetAt(0)), NULL, 10);
	if (m_VersionMajor.GetLength() > 1)
	{
		verMajor = verMajor << 4;
		verMajor |= (BYTE)strtoul((CString)(m_VersionMajor.GetAt(1)), NULL, 10);
	}

	verMinor = (BYTE)strtoul((CString)(m_VersionMinor.GetAt(0)), NULL, 10);
	if (m_VersionMinor.GetLength() > 1)
	{
		verMinor = verMinor << 4;
		verMinor |= (BYTE)strtoul((CString)(m_VersionMinor.GetAt(1)), NULL, 10);
	}

	version = (WORD)((verMajor << 8) | verMinor);

	// Call DLL function for setting Device Release Version.
	CP210x_SetDeviceVersion(m_hUSBDevice, version);
}


void CCP210xSetIDsDlg::SetSerialNumber()
{
	char tmp[1000];
	
	memset(tmp, 0, 1000);
	strcpy(tmp, m_SerNum);

	// Call DLL function for setting serial number string.
	CP210x_SetSerialNumber(m_hUSBDevice, (LPVOID)tmp, m_SerNum.GetLength(), TRUE);
}


void CCP210xSetIDsDlg::SetProductString()
{
	char tmp[1000];
	
	memset(tmp, 0, 1000);
	strcpy(tmp, m_ProdString);

	// Call DLL function for setting product string.
	CP210x_SetProductString(m_hUSBDevice, (LPVOID)tmp, m_ProdString.GetLength(), TRUE);
}


void CCP210xSetIDsDlg::OnSelchangeDeviceSelect() 
{
	CComboBox* pDevList = (CComboBox*)GetDlgItem(IDC_DEVICE_SELECT);

	if (pDevList)
	{
		DWORD	dwNumDevices	= 0;

		m_DeviceCurSel = pDevList->GetCurSel();		// Get index of current selection.
		
		if (m_DeviceCurSel != CB_ERR)
		{
			CP210x_STATUS status = CP210x_GetNumDevices(&dwNumDevices);

			// If index is in range of number of actual devices, get the get strings for 
			// device # x.
			if (status == CP210x_SUCCESS && m_DeviceCurSel < dwNumDevices)
			{
				// Open the device
				if ((status = CP210x_Open(m_DeviceCurSel, &m_hUSBDevice)) == CP210x_SUCCESS)
				{
					CP210x_PRODUCT_STRING	prodStr;
					CP210x_SERIAL_STRING	serialStr;
					BYTE					length	= 0;
					WORD					wVid, wPid, wVersion;
					BYTE					bPower;
					BOOL					bSelfPower;
					BYTE					bPartNum;
					BYTE					bLockValue;
					char					buffer[10];

					// Get the product string first
					if ((status = CP210x_GetDeviceProductString(m_hUSBDevice, prodStr, &length, TRUE)) == CP210x_SUCCESS)
					{
						prodStr[length]	= '\0';
						m_ProdString	= prodStr;

						if ((status = CP210x_GetDeviceSerialNumber(m_hUSBDevice, serialStr, &length, TRUE)) == CP210x_SUCCESS)
						{
							serialStr[length]	= '\0';
							m_SerNum			= serialStr;

							if ((status = CP210x_GetDeviceVid(m_hUSBDevice, &wVid)) == CP210x_SUCCESS)
							{
								memset(buffer, 0, 10);
								itoa(wVid, buffer, 16);
								m_VID = buffer;

								if ((status = CP210x_GetDevicePid(m_hUSBDevice, &wPid)) == CP210x_SUCCESS)
								{
									memset(buffer, 0, 10);
									itoa(wPid, buffer, 16);
									m_PID = buffer;

									if ((status = CP210x_GetSelfPower(m_hUSBDevice, &bSelfPower)) == CP210x_SUCCESS)
									{
										// Set Self-powered checkbox if bit is set
										if (bSelfPower)
										{
											((CButton*)GetDlgItem(IDC_CHECK_SELF_POWERED))->SetCheck(1);
										}
										else
										{
											((CButton*)GetDlgItem(IDC_CHECK_SELF_POWERED))->SetCheck(0);
										}

										if ((status = CP210x_GetMaxPower(m_hUSBDevice, &bPower)) == CP210x_SUCCESS)
										{
											memset(buffer, 0, 10);
											itoa(bPower, buffer, 16);
											m_Power = buffer;

											if ((status = CP210x_GetDeviceVersion(m_hUSBDevice, &wVersion)) == CP210x_SUCCESS)
											{
												BYTE bVer;

												// Extract major version
												memset(buffer, 0, 10);
												bVer = ((BYTE)((wVersion >> 8) & 0x00F0) >> 4) * 10;
												bVer += (BYTE)((wVersion >> 8) & 0x000F);
												itoa(bVer, buffer, 10);
												m_VersionMajor = buffer;
												// Extract minor version
												memset(buffer, 0, 10);
												bVer = ((BYTE)(wVersion & 0x00F0) >> 4) * 10;
												bVer += (BYTE)(wVersion & 0x000F);
												itoa(bVer, buffer, 10);
												m_VersionMinor = buffer;
												
												if ((status = CP210x_GetPartNumber(m_hUSBDevice, &bPartNum)) == CP210x_SUCCESS)
												{
													switch (bPartNum)
													{
													case CP210x_CP2101_VERSION :	m_PartNum = "CP2101"; 
																((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->SetCheck(0);
																((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->SetCheck(0);
																((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->EnableWindow(false);
																((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->EnableWindow(false);
																break;
													case CP210x_CP2102_VERSION : m_PartNum = "CP2102"; 
																if ((status = CP210x_GetLockValue(m_hUSBDevice, &bLockValue)) == CP210x_SUCCESS)
																{
																	((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->EnableWindow(true);
																	((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->EnableWindow(true);
																	((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->SetCheck(bLockValue);
																}
																break;
													case CP210x_CP2103_VERSION : m_PartNum = "CP2103"; 
																if ((status = CP210x_GetLockValue(m_hUSBDevice, &bLockValue)) == CP210x_SUCCESS)
																{
																	((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->EnableWindow(true);
																	((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->EnableWindow(true);
																	((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->SetCheck(bLockValue);
																}
																break;
													}
												}
											}
										}
									}
								}
							}
						}
					}

					UpdateData(FALSE);
				}

				status = CP210x_Close(m_hUSBDevice);

				m_hUSBDevice = INVALID_HANDLE_VALUE;
			}
		}
		else
		{
			// Unset all fields in the application
			m_ProdString = "";
			m_SerNum = "";
			m_VID = "";
			m_PID = "";
			((CButton*)GetDlgItem(IDC_CHECK_SELF_POWERED))->SetCheck(0);
			m_Power = "";
			m_VersionMajor = "";
			m_VersionMinor = "";
			((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->SetCheck(0);
			((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->SetCheck(0);
			((CButton*)GetDlgItem(IDC_CHECK_LOCKVAL))->EnableWindow(false);
			((CButton*)GetDlgItem(IDC_UPDATE_LOCKATTRIB))->EnableWindow(false);
			m_PartNum = "";
			UpdateData(FALSE);
		}
	}
}
