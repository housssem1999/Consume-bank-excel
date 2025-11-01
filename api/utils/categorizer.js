// Auto-categorization logic based on transaction description
const Category = require('../models/Category');

const categoryKeywords = {
  'Food & Dining': ['grocery', 'food', 'restaurant', 'cafe', 'coffee', 'lunch', 'dinner', 'breakfast', 'pizza', 'burger', 'mcdonald', 'starbucks', 'subway'],
  'Transportation': ['gas', 'fuel', 'uber', 'lyft', 'taxi', 'metro', 'bus', 'train', 'parking', 'transit'],
  'Shopping': ['amazon', 'walmart', 'target', 'shop', 'store', 'mall', 'clothing', 'fashion'],
  'Bills & Utilities': ['electric', 'water', 'gas bill', 'internet', 'phone', 'mobile', 'utility'],
  'Entertainment': ['movie', 'cinema', 'netflix', 'spotify', 'game', 'concert', 'theater'],
  'Healthcare': ['doctor', 'hospital', 'pharmacy', 'medical', 'health', 'clinic', 'prescription'],
  'Income': ['salary', 'payroll', 'income', 'deposit', 'payment received', 'transfer in'],
  'Transfer': ['transfer', 'atm', 'withdrawal', 'cash'],
  'Housing': ['rent', 'mortgage', 'lease', 'property'],
  'Insurance': ['insurance', 'policy', 'premium']
};

async function categorizeByDescription(description, userId) {
  const descLower = description.toLowerCase();
  
  // Try to match keywords
  for (const [categoryName, keywords] of Object.entries(categoryKeywords)) {
    for (const keyword of keywords) {
      if (descLower.includes(keyword)) {
        // Find or create category
        let category = await Category.findOne({ 
          name: categoryName,
          $or: [{ user: userId }, { user: null }]
        });
        
        if (!category) {
          // Try to find system default category
          category = await Category.findOne({ 
            name: categoryName,
            user: null
          });
        }
        
        if (category) {
          return category;
        }
      }
    }
  }
  
  // Default to "Other" category if no match found
  let otherCategory = await Category.findOne({ 
    name: 'Other',
    $or: [{ user: userId }, { user: null }]
  });
  
  if (!otherCategory) {
    otherCategory = await Category.findOne({ name: 'Other', user: null });
  }
  
  return otherCategory;
}

module.exports = {
  categorizeByDescription,
  categoryKeywords
};
