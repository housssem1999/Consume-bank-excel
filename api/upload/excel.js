const { connectToDatabase } = require('../db');
const { authenticateUser } = require('../middleware/auth');
const Transaction = require('../models/Transaction');
const { categorizeByDescription } = require('../utils/categorizer');
const XLSX = require('xlsx');

module.exports = async (req, res) => {
  if (req.method === 'OPTIONS') {
    return res.status(200).json({ success: true });
  }

  if (req.method !== 'POST') {
    return res.status(405).json({ success: false, message: 'Method not allowed' });
  }

  try {
    await connectToDatabase();
    const user = await authenticateUser(req);

    // Get file from request body (base64 encoded)
    const { file, filename } = req.body;

    if (!file) {
      return res.status(400).json({
        success: false,
        message: 'Please select a file to upload'
      });
    }

    // Check file type
    if (!filename || (!filename.endsWith('.xlsx') && !filename.endsWith('.xls'))) {
      return res.status(400).json({
        success: false,
        message: 'Please upload a valid Excel file (.xlsx or .xls)'
      });
    }

    // Decode base64 file
    const buffer = Buffer.from(file, 'base64');

    // Parse Excel file
    const workbook = XLSX.read(buffer, { type: 'buffer' });
    const sheetName = workbook.SheetNames[0];
    const worksheet = workbook.Sheets[sheetName];

    // Convert to JSON
    const rawData = XLSX.utils.sheet_to_json(worksheet, { header: 1 });

    if (rawData.length < 2) {
      return res.status(400).json({
        success: false,
        message: 'Excel file must contain at least a header row and one data row'
      });
    }

    // Skip header row
    const dataRows = rawData.slice(1);
    const transactions = [];

    for (const row of dataRows) {
      if (!row || row.length < 3) continue; // Skip empty or incomplete rows

      const [dateValue, description, amountValue, reference] = row;

      if (!dateValue || !description || amountValue === undefined) continue;

      // Parse date
      let date;
      if (typeof dateValue === 'number') {
        // Excel date serial number
        date = new Date((dateValue - 25569) * 86400 * 1000);
      } else if (typeof dateValue === 'string') {
        date = new Date(dateValue);
      } else {
        continue;
      }

      // Parse amount
      const amount = typeof amountValue === 'number' ? amountValue : parseFloat(amountValue);
      if (isNaN(amount)) continue;

      // Determine transaction type
      const type = amount >= 0 ? 'INCOME' : 'EXPENSE';

      // Auto-categorize
      const category = await categorizeByDescription(description, user._id);

      // Create transaction
      const transaction = new Transaction({
        date,
        description: String(description).trim(),
        amount: Math.abs(amount),
        type,
        category: category ? category._id : null,
        user: user._id,
        reference: reference ? String(reference).trim() : undefined
      });

      await transaction.save();
      transactions.push(transaction);
    }

    return res.status(200).json({
      success: true,
      message: 'File uploaded and processed successfully',
      transactionsProcessed: transactions.length,
      transactions: transactions.map(t => ({
        id: t._id,
        date: t.date,
        description: t.description,
        amount: t.amount,
        type: t.type,
        reference: t.reference
      }))
    });

  } catch (error) {
    console.error('Error processing Excel file:', error);
    return res.status(500).json({
      success: false,
      message: 'Error processing file: ' + error.message
    });
  }
};
